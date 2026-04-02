#!/usr/bin/env bash
# axios/plain-crypto-js supply chain attack - affected machine check
# Covers: CVE axios@1.14.1, axios@0.30.4, plain-crypto-js@4.2.1
# IOCs sourced from: StepSecurity, SANS, Malwarebytes, Google GTIG (2026-03-31)
# IMPORTANT: Malware self-deletes after execution. A positive on the PACKAGE
# check alone is sufficient to treat the machine as compromised — do not
# wait for IOC files to confirm. Rotate all credentials immediately.
#
# Usage: ./test.sh [scan-root]
#   scan-root  Directory to scan for JS projects (default: current directory)
#              Use $HOME to scan all projects on the machine.

SCAN_ROOT="${1:-.}"
if [ ! -d "$SCAN_ROOT" ]; then
  echo "ERROR: $SCAN_ROOT is not a directory" >&2
  exit 1
fi

echo "================================================================"
echo " axios supply chain attack — affected machine check"
echo " $(date -u '+%Y-%m-%dT%H:%M:%SZ')"
echo " Scan root: $(cd "$SCAN_ROOT" && pwd)"
echo "================================================================"
echo ""
echo "NOTE: This script flags indicators for review — not a definitive"
echo "      diagnosis. Verify all findings before taking action."
echo "      IOC file absence does not clear a machine (dropper self-deletes)."
echo ""

overall=0

# ── Progress spinner ─────────────────────────────────────────────────────────
# Usage: spin_start "message" → sets SPIN_PID
#        spin_stop             → kills spinner, clears line
spin_start() {
  local msg="$1"
  (
    local chars='⠋⠙⠹⠸⠼⠴⠦⠧⠇⠏'
    local i=0 elapsed=0
    while true; do
      printf '\r  %s %s (%ds)' "${chars:$((i % ${#chars})):1}" "$msg" "$elapsed" >&2
      sleep 0.2
      i=$((i + 1))
      elapsed=$((i / 5))
    done
  ) &
  SPIN_PID=$!
}

spin_stop() {
  if [ -n "$SPIN_PID" ]; then
    kill "$SPIN_PID" 2>/dev/null
    wait "$SPIN_PID" 2>/dev/null
    printf '\r\033[K' >&2
    SPIN_PID=""
  fi
}
trap spin_stop EXIT

# ── Prune directories with no chance of containing JS project files ──────────
# These are skipped entirely by find (no descent), making $HOME scans feasible.
# Only prune dirs that structurally cannot contain a JS project:
#   VCS internals, package manager caches, language toolchain stores
PRUNE_NAMES=(
  .git .hg .svn
  .cache .npm .nvm .pnpm-store .yarn
  .Trash .docker
  .cargo .rustup .gradle .m2 .pyenv .rbenv
  .terraform
)
# macOS: ~/Library is frameworks/caches, not code
if [[ "$(uname)" == "Darwin" ]]; then
  PRUNE_NAMES+=( Library )
fi
# Build find expression
_prune_expr=()
for _d in "${PRUNE_NAMES[@]}"; do
  [ ${#_prune_expr[@]} -gt 0 ] && _prune_expr+=( -o )
  _prune_expr+=( -name "$_d" )
done
PRUNE=( -type d \( "${_prune_expr[@]}" \) -prune )

# ── 1. Package manifest / lockfile scan ──────────────────────────────────────
# Classification:
#   CONFIRMED         — exact malicious version resolved/pinned
#   POTENTIAL EXPOSURE — semver range that could resolve to the malicious version
#   SAFE              — pinned to a version that definitely cannot reach it
#
# Malicious versions: axios@1.14.1, axios@0.30.4, plain-crypto-js@4.2.1
# Secondary package:  @qqbrowser/openclaw-qbot@0.0.130

# semver_compare: returns 0 if $1 <= $2 (both in X.Y.Z form)
semver_lte() {
  local a_major a_minor a_patch b_major b_minor b_patch
  IFS='.' read -r a_major a_minor a_patch <<< "$1"
  IFS='.' read -r b_major b_minor b_patch <<< "$2"
  a_major="${a_major:-0}"; a_minor="${a_minor:-0}"; a_patch="${a_patch:-0}"
  b_major="${b_major:-0}"; b_minor="${b_minor:-0}"; b_patch="${b_patch:-0}"
  if (( a_major < b_major )); then return 0; fi
  if (( a_major > b_major )); then return 1; fi
  if (( a_minor < b_minor )); then return 0; fi
  if (( a_minor > b_minor )); then return 1; fi
  if (( a_patch <= b_patch )); then return 0; fi
  return 1
}
# strict less-than
semver_lt() {
  if [[ "$1" == "$2" ]]; then return 1; fi
  semver_lte "$1" "$2"
}

# can_reach_target <lower> <upper_exclusive> <target>
# Returns 0 if target is in [lower, upper_exclusive)
can_reach_target() {
  local lower="$1" upper="$2" target="$3"
  semver_lte "$lower" "$target" && semver_lt "$target" "$upper"
}

# classify_axios_range <version-specifier>
# Prints: CONFIRMED | POTENTIAL_EXPOSURE | SAFE
#
# Malicious versions: axios@1.14.1 and axios@0.30.4
# Logic: determine the effective [lower, upper) range the specifier permits,
# then check whether either malicious version falls within it.
classify_axios_range() {
  local spec="$1"
  # Strip leading/trailing whitespace
  spec="$(echo "$spec" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"

  # ── Exact malicious versions ──
  if [[ "$spec" == "1.14.1" || "$spec" == "0.30.4" ]]; then
    echo "CONFIRMED"; return
  fi

  # ── Wildcard / latest / bare star — unbounded, always exposed ──
  if [[ "$spec" == "latest" || "$spec" == "*" || "$spec" == "x" ]]; then
    echo "POTENTIAL_EXPOSURE"; return
  fi

  # ── Bare exact version (no operator) that isn't malicious — safe ──
  if echo "$spec" | grep -qE '^[0-9]+\.[0-9]+\.[0-9]+$'; then
    echo "SAFE"; return
  fi

  # ── X-ranges: 1, 1.x, 1.*, 0.30, 0.30.x, 0.30.* ──
  # "1" or "1.x" or "1.*" → >=1.0.0 <2.0.0
  if echo "$spec" | grep -qE '^1(\.x|\.\*)?$'; then
    echo "POTENTIAL_EXPOSURE"; return  # contains 1.14.1
  fi
  # "0.30" or "0.30.x" or "0.30.*" → >=0.30.0 <0.31.0
  if echo "$spec" | grep -qE '^0\.30(\.x|\.\*)?$'; then
    echo "POTENTIAL_EXPOSURE"; return  # contains 0.30.4
  fi
  # "0" or "0.x" or "0.*" → >=0.0.0 <1.0.0 — contains 0.30.4
  if echo "$spec" | grep -qE '^0(\.x|\.\*)?$'; then
    echo "POTENTIAL_EXPOSURE"; return
  fi

  # ── Caret ranges ──
  # ^major.minor.patch:
  #   major>0 → >=major.minor.patch <(major+1).0.0
  #   major=0, minor>0 → >=0.minor.patch <0.(minor+1).0
  #   major=0, minor=0 → >=0.0.patch <0.0.(patch+1)
  if echo "$spec" | grep -qE '^\^'; then
    local body="${spec#^}"
    local major minor patch
    IFS='.' read -r major minor patch <<< "$body"
    major="${major:-0}"; minor="${minor:-0}"; patch="${patch:-0}"
    local lower="$major.$minor.$patch"
    local upper
    if (( major > 0 )); then
      upper="$((major + 1)).0.0"
    elif (( minor > 0 )); then
      upper="0.$((minor + 1)).0"
    else
      upper="0.0.$((patch + 1))"
    fi
    if can_reach_target "$lower" "$upper" "1.14.1" || \
       can_reach_target "$lower" "$upper" "0.30.4"; then
      echo "POTENTIAL_EXPOSURE"; return
    fi
    echo "SAFE"; return
  fi

  # ── Tilde ranges ──
  # ~major.minor.patch → >=major.minor.patch <major.(minor+1).0
  if echo "$spec" | grep -qE '^~'; then
    local body="${spec#\~}"
    local major minor patch
    IFS='.' read -r major minor patch <<< "$body"
    major="${major:-0}"; minor="${minor:-0}"; patch="${patch:-0}"
    local lower="$major.$minor.$patch"
    local upper="$major.$((minor + 1)).0"
    if can_reach_target "$lower" "$upper" "1.14.1" || \
       can_reach_target "$lower" "$upper" "0.30.4"; then
      echo "POTENTIAL_EXPOSURE"; return
    fi
    echo "SAFE"; return
  fi

  # ── >= operator (unbounded upper) ──
  # >=X.Y.Z → can reach any version >= X.Y.Z
  if echo "$spec" | grep -qE '^>='; then
    local body="${spec#>=}"
    # Normalize partial versions: >=1 → 1.0.0, >=0.30 → 0.30.0
    local major minor patch
    IFS='.' read -r major minor patch <<< "$body"
    major="${major:-0}"; minor="${minor:-0}"; patch="${patch:-0}"
    local lower="$major.$minor.$patch"
    # Exposed if lower <= either malicious version
    if semver_lte "$lower" "1.14.1" || semver_lte "$lower" "0.30.4"; then
      echo "POTENTIAL_EXPOSURE"; return
    fi
    echo "SAFE"; return
  fi

  # ── > operator (strict, unbounded upper) ──
  if echo "$spec" | grep -qE '^>[0-9]'; then
    local body="${spec#>}"
    local major minor patch
    IFS='.' read -r major minor patch <<< "$body"
    major="${major:-0}"; minor="${minor:-0}"; patch="${patch:-0}"
    local lower="$major.$minor.$patch"
    # Exposed if lower < either malicious version (strict >)
    if semver_lt "$lower" "1.14.1" || semver_lt "$lower" "0.30.4"; then
      echo "POTENTIAL_EXPOSURE"; return
    fi
    echo "SAFE"; return
  fi

  # ── <= operator (bounded upper, lower at 0.0.0) ──
  if echo "$spec" | grep -qE '^<='; then
    local body="${spec#<=}"
    local major minor patch
    IFS='.' read -r major minor patch <<< "$body"
    major="${major:-0}"; minor="${minor:-0}"; patch="${patch:-0}"
    local upper="$major.$minor.$patch"
    # Exposed if either malicious version <= upper
    if semver_lte "1.14.1" "$upper" || semver_lte "0.30.4" "$upper"; then
      echo "POTENTIAL_EXPOSURE"; return
    fi
    echo "SAFE"; return
  fi

  # ── < operator (bounded upper, lower at 0.0.0) ──
  if echo "$spec" | grep -qE '^<[0-9]'; then
    local body="${spec#<}"
    local major minor patch
    IFS='.' read -r major minor patch <<< "$body"
    major="${major:-0}"; minor="${minor:-0}"; patch="${patch:-0}"
    local upper="$major.$minor.$patch"
    # Exposed if either malicious version < upper
    if semver_lt "1.14.1" "$upper" || semver_lt "0.30.4" "$upper"; then
      echo "POTENTIAL_EXPOSURE"; return
    fi
    echo "SAFE"; return
  fi

  # ── Compound / complex ranges (||, space-separated, hyphen ranges) ──
  # Conservative: flag for manual review
  echo "POTENTIAL_EXPOSURE"; return
}

echo "=== [1/5] Package dependency scan ==="
pkg_confirmed=0
pkg_exposure=0
pkg_scanned=0

# ── 1a. Lockfile scan (resolved exact versions = CONFIRMED) ──
spin_start "Scanning lockfiles..."
lockfile_hits="$(find "$SCAN_ROOT" \
  "${PRUNE[@]}" -o \
  \( -name package-lock.json \
     -o -name npm-shrinkwrap.json \
     -o -name yarn.lock \
     -o -name pnpm-lock.yaml \) \
  -not -path '*/node_modules/*' \
  -print0 2>/dev/null \
  | xargs -0 grep -HnE \
    'axios@(1\.14\.1|0\.30\.4)|"resolved".*axios-1\.14\.1|"resolved".*axios-0\.30\.4|'\
'plain-crypto-js@4\.2\.1|"resolved".*plain-crypto-js-4\.2\.1|'\
'@qqbrowser/openclaw-qbot@0\.0\.130' \
    2>/dev/null)"

spin_stop

hits_safe=()

if [ -n "$lockfile_hits" ]; then
  printf '  [CONFIRMED] lockfile: %s\n' "$lockfile_hits"
  pkg_confirmed=1
  overall=1
fi

# ── 1b. package.json scan (semver range classification) ──
# Ranges only appear in package.json, not lockfiles — lockfiles have resolved
# versions already caught by the exact-match check above.
spin_start "Scanning package.json files..."
while IFS= read -r -d '' pjson; do
  [ -n "$SPIN_PID" ] && spin_stop
  ((pkg_scanned++))
  # Extract axios version specifier from dependencies / devDependencies
  axios_specs="$(sed -n 's/.*"axios"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' "$pjson" 2>/dev/null)"
  for spec in $axios_specs; do
    classification="$(classify_axios_range "$spec")"
    case "$classification" in
      CONFIRMED)
        echo "  [CONFIRMED] $pjson — axios@\"$spec\" (exact malicious version)"
        pkg_confirmed=1
        overall=1
        ;;
      POTENTIAL_EXPOSURE)
        echo "  [POTENTIAL EXPOSURE] $pjson — axios@\"$spec\" (range can resolve to malicious version)"
        pkg_exposure=1
        overall=1
        ;;
      SAFE)
        hits_safe+=("  [SAFE] $pjson — axios@\"$spec\"")
        ;;
    esac
  done

  # Direct checks for always-malicious packages (any version = compromised)
  if grep -qE '"plain-crypto-js"' "$pjson" 2>/dev/null; then
    echo "  [CONFIRMED] $pjson — plain-crypto-js found (malicious package)"
    pkg_confirmed=1
    overall=1
  fi
  if grep -qE '"@qqbrowser/openclaw-qbot"' "$pjson" 2>/dev/null; then
    echo "  [CONFIRMED] $pjson — @qqbrowser/openclaw-qbot found (ships plain-crypto-js)"
    pkg_confirmed=1
    overall=1
  fi
done < <(find "$SCAN_ROOT" \
  "${PRUNE[@]}" -o \
  -name package.json \
  -not -path '*/node_modules/*' \
  -print0 2>/dev/null)

spin_stop

# ── 1c. node_modules installed version check (ground truth) ──
# The actual "version" field in node_modules/axios/package.json is what ran.
# This catches cases where the lockfile is missing, stale, or not committed.
spin_start "Scanning node_modules..."
while IFS= read -r -d '' installed; do
  [ -n "$SPIN_PID" ] && spin_stop
  inst_ver="$(sed -n 's/.*"version"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' "$installed" 2>/dev/null | head -1)"
  if [[ "$inst_ver" == "1.14.1" || "$inst_ver" == "0.30.4" ]]; then
    echo "  [CONFIRMED] $installed — axios@$inst_ver installed on disk"
    pkg_confirmed=1
    overall=1
  fi
done < <(find "$SCAN_ROOT" \
  "${PRUNE[@]}" -o \
  -path '*/node_modules/axios/package.json' \
  -print0 2>/dev/null)

spin_stop

# Print safe items last — these are not actionable but useful for audit trail
if [ ${#hits_safe[@]} -gt 0 ]; then
  for line in "${hits_safe[@]}"; do echo "$line"; done
fi
echo "  Scanned $pkg_scanned package.json file(s)"
if [[ $pkg_confirmed -eq 0 && $pkg_exposure -eq 0 ]]; then
  echo "  OK — no affected packages found in lockfiles or package.json"
elif [[ $pkg_confirmed -eq 1 ]]; then
  echo ""
  echo "  !! CONFIRMED — malicious version referenced in this project."
  echo "     To be affected, a resolve must have happened during the exposure"
  echo "     window (2026-03-31 00:21–03:15 UTC / 05:51–08:45 IST). This means:"
  echo "       - 'npm install' without a lockfile (resolves latest matching)"
  echo "       - 'npm update' (re-resolves within semver range)"
  echo "       - Fresh CI/CD build with no lockfile committed"
  echo "     If a lockfile existed and no update was run, npm install would"
  echo "     have used the previously pinned version — not the malicious one."
  echo "     Check CI/CD logs and deployment timestamps for that window."
elif [[ $pkg_exposure -eq 1 ]]; then
  echo ""
  echo "  !! POTENTIAL EXPOSURE — semver range could resolve to malicious version."
  echo "     Same verification applies — only affected if a resolve happened"
  echo "     during the window (2026-03-31 00:21–03:15 UTC / 05:51–08:45 IST):"
  echo "       - 'npm install' without a lockfile"
  echo "       - 'npm update'"
  echo "       - Fresh CI/CD build with no lockfile committed"
  echo "     If a lockfile was present and pinned a safe version, npm install"
  echo "     would not have pulled the malicious version."
  echo "     Check the resolved version in your lockfile or"
  echo "     node_modules/axios/package.json."
fi
echo ""

# ── 2. node_modules installation directory check ─────────────────────────────
# plain-crypto-js directory may still exist even after the malware self-deletes
# its setup.js and replaces package.json with a clean stub (per SANS advisory).
echo "=== [2/5] node_modules/plain-crypto-js directory check ==="
spin_start "Scanning for plain-crypto-js installations..."
npm_dir="$(find "$SCAN_ROOT" \
  "${PRUNE[@]}" -o \
  -type d -name plain-crypto-js -path '*/node_modules/plain-crypto-js' \
  -print 2>/dev/null)"
spin_stop
if [ -n "$npm_dir" ]; then
  printf '%s\n' "$npm_dir"
  echo ""
  echo "  !! plain-crypto-js directory found — verify contents and investigate"
  overall=1
else
  echo "  OK — no plain-crypto-js directory found under node_modules"
fi
echo ""

# ── 3. RAT artifact (persistent file) check ──────────────────────────────────
# macOS: AppleScript payload writes a Mach-O binary mimicking an Apple daemon.
# Linux: Python RAT fetched to /tmp/ld.py, cron persistence in /tmp/.cron_* ,
#        systemd user service in ~/.config/systemd/user/
# Windows covered separately below — not checkable from bash.
echo "=== [3/5] RAT artifact (IOC file) check ==="
ioc_paths=( "/tmp/ld.py" )
if [[ "$(uname)" == "Darwin" ]]; then
  ioc_paths+=( "/Library/Caches/com.apple.act.mond" )
else
  # Linux-specific persistence locations
  ioc_paths+=(
    "/tmp/.cron_update"
    "$HOME/.config/systemd/user/node-update.service"
  )
fi
ioc=0
for ioc_path in "${ioc_paths[@]}"; do
  if [ -e "$ioc_path" ]; then
    ls -la "$ioc_path"
    ioc=1
    overall=1
  fi
done
if [ $ioc -eq 1 ]; then
  echo ""
  echo "  !! IOC file found — verify and escalate to security team"
else
  echo "  OK — no RAT artifact files found"
  echo "       (absence is not clearance — dropper self-deletes)"
fi
echo ""

# ── 4. Shell profile persistence check ───────────────────────────────────────
# The RAT attempts persistence via shell profile injection (.bashrc, .zshrc, etc).
# Check for C2 domain or known artifact names written into profile files.
echo "=== [4/5] Shell profile persistence check ==="
profile_hit=0
for profile in \
  "$HOME/.bashrc" "$HOME/.zshrc" "$HOME/.bash_profile" \
  "$HOME/.profile" "$HOME/.zprofile" "$HOME/.config/fish/config.fish"; do
  if [ -f "$profile" ]; then
    if grep -qE 'sfrclak|ld\.py|com\.apple\.act\.mond|plain-crypto-js' "$profile" 2>/dev/null; then
      echo "  !! Suspicious entry in $profile — review manually:"
      grep -nE 'sfrclak|ld\.py|com\.apple\.act\.mond|plain-crypto-js' "$profile"
      profile_hit=1
      overall=1
    fi
  fi
done
if [ $profile_hit -eq 0 ]; then
  echo "  OK — no C2/artifact references found in shell profiles"
fi
echo ""

# ── 5. Shell history C2 artefact check ───────────────────────────────────────
# If the dropper ran, curl/wget calls to sfrclak.com:8000 may appear in history.
# Only match the C2 domain — package names like "plain-crypto-js" trigger false
# positives from anyone investigating the incident.
echo "=== [5/5] Shell history C2 reference check ==="
hist_hit=0
for hist in "$HOME/.bash_history" "$HOME/.zsh_history"; do
  if [ -f "$hist" ]; then
    if grep -qE 'sfrclak' "$hist" 2>/dev/null; then
      echo "  !! C2 domain reference found in $hist — review context:"
      grep -nE 'sfrclak' "$hist"
      hist_hit=1
      overall=1
    fi
  fi
done
if [ $hist_hit -eq 0 ]; then
  echo "  OK — no C2 references found in shell history"
fi
echo ""

# ── Summary ───────────────────────────────────────────────────────────────────
echo "================================================================"
if [ $overall -eq 1 ]; then
  echo " RESULT: INDICATORS FOUND — verification required"
  echo ""
  echo " Recommended next steps:"
  echo "   1. Verify findings above — confirm resolved versions in lockfiles"
  echo "      and node_modules before escalating"
  echo "   2. If confirmed compromised, engage your security/incident team"
  echo "   3. Audit CI/CD runs between 2026-03-31 00:21–03:15 UTC / 05:51–08:45 IST"
  echo "   4. Check for C2 traffic: sfrclak[.]com / 142.11.206.73:8000"
  echo "   5. Credential rotation scope should be determined by your"
  echo "      security team based on verified exposure"
else
  echo " RESULT: No indicators found on this machine ($(uname))"
  echo "         Re-run on all developer machines and CI runners"
fi
echo ""
echo " Windows machines must be checked separately:"
echo "   %PROGRAMDATA%\\wt.exe"
echo "   %TEMP%\\6202033.vbs  /  %TEMP%\\6202033.ps1"
echo ""
echo " Full IOC list:"
echo "   Domain : sfrclak[.]com"
echo "   IP     : 142.11.206.73 (port 8000)"
echo "   Hashes : axios@1.14.1       2553649f232204966871cea80a5d0d6adc700ca"
echo "            axios@0.30.4       d6f3f62fd3b9f5432f5782b62d8cfd5247d5ee71"
echo "            plain-crypto-js@4.2.1  07d889e2dadce6f3910dcbc253317d28ca61c766"
echo "================================================================"