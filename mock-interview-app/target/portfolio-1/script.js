// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


/*async function login() {
  const response = await fetch('/login');
  const hidelogin = document.getElementById("hidelogin");
  const showlogout = document.getElementById("showlogout");
  const user = await response.json();
   if (user["useremail"] == ""){
    hidelogin.style.display = "block";
    showlogout.style.display ="none";
 }else{
    showlogout.style.display= "block";
    hidelogin.style.display= "none";
 }
  const loginform = document.getElementById('loginform');
  var str = "l";
  console.log(str.link(user['url'])); 
  console.log(user['url']);
  loginform.action = user['url'];
  
}*/
async function login() {
  const response = await fetch('/login');
  const user = await response.text();
  const username = user.split("<p");
  const hidelogin = document.getElementById("hidelogin");
  const showlogout = document.getElementById("showlogout");
  if (username[0].includes("stranger please sign in")){
    hidelogin.style.display = "block";
    showlogout.style.display ="none";
   }else{
    showlogout.style.display= "block";
    hidelogin.style.display= "none";
   }
}