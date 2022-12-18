"""
See API doc: https://github.com/ssllabs/ssllabs-scan/blob/master/ssllabs-api-docs.md
"""
import json
import logging
import os
import time
from datetime import datetime

import requests

logging.getLogger().setLevel(logging.INFO)

API_URL = "https://api.ssllabs.com/api/v3/analyze"

CHAIN_ISSUES = {
    "0": "none",
    "1": "unused",
    "2": "incomplete chain",
    "4": "chain contains unrelated or duplicate certificates",
    "8": "the certificates form a chain (trusted or not) but incorrect order",
    "16": "contains a self-signed root certificate",
    "32": "the certificates form a chain but cannot be validated",
}

# Forward secrecy protects past sessions against future compromises of secret keys or passwords.
FORWARD_SECRECY = {
    "1": "With some browsers WEAK",
    "2": "With modern browsers",
    "4": "Yes (with most browsers) ROBUST",
}

PROTOCOLS = [
    "TLS 1.3", "TLS 1.2", "TLS 1.1", "TLS 1.0", "SSL 3.0 INSECURE", "SSL 2.0 INSECURE"
]

RC4 = ["Support RC4", "RC4 with modern protocols", "RC4 Only"]

VULNERABLES = [
    "Vuln Beast", "Vuln Drown", "Vuln Heartbleed", "Vuln FREAK",
    "Vuln openSsl Ccs", "Vuln openSSL LuckyMinus20", "Vuln POODLE", "Vuln POODLE TLS"
]

SUMMARY_COL_NAMES = [
    "Host", "Grade", "HasWarnings", "Cert Expiry", "Chain Status", "Forward Secrecy", "Heartbeat ext"
] + VULNERABLES + RC4 + PROTOCOLS


class SSLLabsClient():
    def __init__(self, check_progress_interval_secs=30, max_attempts=100, verify=True):
        self._check_progress_interval_secs = check_progress_interval_secs
        self._max_attempts = max_attempts
        self._verify = verify

    def analyze(self, host, summary_csv_file):
        data = self.start_new_scan(host=host)
        if data.get("status") == "ERROR":
            return

        # write the output to file
        json_file = os.path.join(os.path.dirname(summary_csv_file), f"{host}.json")
        with open(json_file, "w") as outfile:
            json.dump(data, outfile, indent=2)

        # write the summary to file
        self.append_summary_csv(summary_csv_file, host, data)

    def start_new_scan(self, host, publish="off", startNew="off", all="done", ignoreMismatch="on"):
        path = API_URL
        payload = {
            "host": host,
            "publish": publish,
            "startNew": startNew,
            "all": all,
            "ignoreMismatch": ignoreMismatch
        }

        response = self.request_api(path, payload)
        results = response.json()

        payload.pop("startNew")

        # status - assessment status; possible values: DNS, ERROR, IN_PROGRESS, and READY.
        while response.status_code == 200 and results["status"] not in ["READY", "ERROR"]:
            self.print_msg(response, "WAIT_FOR_COMPLETE")
            time.sleep(self._check_progress_interval_secs)
            response = self.request_api(path, payload)
            results = response.json()

        if response.status_code != 200 or results["status"] == "ERROR":
            self.print_msg(response, "FAILED_AND_SKIPPED", host)
            return {"status": "ERROR", "statusMessage": results.get("statusMessage")}

        return results

    def request_api(self, url, payload):
        response = self.requests_get(url, payload)
        attempts = 0

        # Supported error codes
        #   400  # invocation error (e.g., invalid parameters)
        #   429  # client request rate too high or too many new assessments too fast
        #   500  # internal error
        #   503  # the service is not available (e.g., down for maintenance)
        #   529  # the service is overloaded
        # See https://github.com/ssllabs/ssllabs-scan/blob/master/ssllabs-api-docs-v3.md

        while response.status_code in [429, 529] and attempts < self._max_attempts:
            self.print_msg(response, "WAIT_FOR_RETRY")
            attempts += 1
            time.sleep(self._check_progress_interval_secs)
            response = self.requests_get(url, payload)

        return response

    def requests_get(self, url, payload):
        return requests.get(url, params=payload, verify=self._verify)

    @staticmethod
    def prepare_datetime(epoch_time):
        # SSL Labs returns an 13-digit epoch time that contains milliseconds, Python only expects 10 digits (seconds)
        return datetime.utcfromtimestamp(float(str(epoch_time)[:10])).strftime("%Y-%m-%d")

    def append_summary_csv(self, summary_file, host, data):
        # write the summary to file
        with open(summary_file, "a") as outfile:
            na = self.prepare_datetime(data["certs"][0]["notAfter"])
            for ep in data["endpoints"]:
                # Skip endpoints that were not contactable during the scan (e.g. GitHub Pages URLs with IPv6 endpoints)
                if "Unable" in ep.get("statusMessage", ""):
                    continue
                # see SUMMARY_COL_NAMES
                summary = [
                    host,
                    ep["grade"],
                    ep["hasWarnings"],
                    na,
                    CHAIN_ISSUES[str(ep["details"]["certChains"][0]["issues"])],
                    FORWARD_SECRECY[str(ep["details"]["forwardSecrecy"])],
                    ep["details"]["heartbeat"],
                    ep["details"]["vulnBeast"],
                    ep["details"]["drownVulnerable"],
                    ep["details"]["heartbleed"],
                    ep["details"]["freak"],
                    False if ep["details"]["openSslCcs"] == 1 else True,
                    False if ep["details"]["openSSLLuckyMinus20"] == 1 else True,
                    ep["details"]["poodle"],
                    False if ep["details"]["poodleTls"] == 1 else True,
                    ep["details"]["supportsRc4"],
                    ep["details"]["rc4WithModern"],
                    ep["details"]["rc4Only"],
                ]
                for protocol in PROTOCOLS:
                    found = False
                    for p in ep["details"]["protocols"]:
                        if protocol.startswith(f"{p['name']} {p['version']}"):
                            found = True
                            break
                    summary += ["Yes" if found is True else "No"]

                outfile.write(",".join(str(s) for s in summary) + "\n")

    def print_msg(self, response, msg_type, host=None):
        results = response.json()
        status_code = response.status_code
        status = results["status"]
        status_msg = results.get("statusMessage")

        if msg_type == "WAIT_FOR_COMPLETE":
            msg = f"Status: {status}, StatusMsg({status_msg}): waiting {self._check_progress_interval_secs} secs until next check..."
        elif msg_type == "WAIT_FOR_RETRY":
            msg = f"Error on requesting API: StatusCode({status_code}), Status({status}), StatusMsg({status_msg}). " \
                  f"Waiting {self._check_progress_interval_secs} secs until next retry..."
        elif msg_type == "FAILED_AND_SKIPPED":
            msg = f"Failed to process ({host}): StatusCode({status_code}), Status({status}), StatusMsg({status_msg}). Skip this host."
        else:
            msg = f"StatusCode({status_code}), Status({status}), StatusMsg({status_msg})"

        if msg_type == "DEBUG":
            logging.info(msg)
        else:
            print(msg)
