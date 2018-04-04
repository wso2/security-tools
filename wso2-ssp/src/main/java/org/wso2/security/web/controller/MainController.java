/*
 *
 *  *  Copyright (c) ${YEAR} WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  *  WSO2 Inc. licenses this file to you under the Apache License,
 *  *  Version 2.0 (the "License"); you may not use this file except
 *  *  in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *  http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing,
 *  *  software distributed under the License is distributed on an
 *  *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  *  KIND, either express or implied.  See the License for the
 *  *  specific language governing permissions and limitations
 *  *  under the License.
 *  *
 *
 */

package org.wso2.security.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wso2.security.web.entity.User;
import org.wso2.security.web.repository.UserRepository;


import javax.servlet.http.HttpServletRequest;


@Controller
public class MainController {

	@Autowired
	private UserRepository userRepository;

	private static final Logger log = LoggerFactory.getLogger(MainController.class);


		@RequestMapping("/index")
		public String getIndex(Model model) {
			return "general/index";
		}

		@RequestMapping("/dashboard")
		public String getDashboard(Model model, HttpServletRequest request) {

			log.info("\nUser " + request.getRemoteAddr() + " is trying to access the server");

			return "general/index";
		}

		@RequestMapping("/fine-upload")
		public String getFileUpload(Model model) {
			return "filehandling/uploadBinaryFile";
		}

		@RequestMapping(value = "/add-component-details" , method = RequestMethod.POST)
		@ResponseBody
		public String addComponentDetails(Model model , @RequestParam String username
				, @RequestParam String email, @RequestParam String team,@RequestParam String componentname) {


			return "success";
		}

	
	@GetMapping(path = "/all")
	public @ResponseBody Iterable<User> getAllUsers() {
		// This returns a JSON or XML with the users
		return userRepository.findAll();
	}
}
