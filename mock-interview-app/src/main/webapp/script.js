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

async function getInterviewRequests(language) {
    const queryString = window.location.search;
    const urlParams = new URLSearchParams(queryString);
    const key = urlParams.get('key');
    let response;
    if(key != null) {
        response = await fetch('/data?key=' + key);
    }
    else {
        response = await fetch('/data');
    }
    const listings = await response.json();
    let html = "";
    for(const listing of listings) {
        if(language != "None" && listing.programmingLanguage != language)
            continue;
        html += `<div class="listing">`;
        html += `<p><b>Preferred Topic (If Any):</b> ${listing.topic}</p>`;
        html += `<p><b>Preferred Spoken Language:</b> ${listing.spokenLanguage}</p>`;
        html += `<p><b>Programming Language:</b> ${listing.programmingLanguage}</p>`;
        html += `<p><b>Hangouts meeting:</b> ${listing.communicationURL}</p>`;
        html += `<p><b>Programming environment:</b> ${listing.environmentURL}</p>`;
        let timesAvailable = "";
        for(const time of listing.timesAvailable) {
            timesAvailable += time + " ";
        }
        html += `<p><b>Time Availability:</b> ${timesAvailable}</p>`;

        html += `<a href="InterviewRequestDetails.html?key=${listing.key}">Read More</a>`;
        html += `</div>`;
    }
    document.getElementById('interview-listings').innerHTML = html;
}

async function login() {
  const response = await fetch('/login');
  const loginResponse = await response.json();
  
  const buttonText = loginResponse.loggedIn ? 'Logout' : 'Login';
  const url = loginResponse.url;

  $("#home-button").text(buttonText);
  $("#home-button").attr("href",url);
  console.log(url);
}

async function getSections() {
  const response = await fetch('/login');
  const loginResponse = await response.json();
  if (loginResponse.loggedIn)
    $("#header-placeholder").load("header.html");
  $("#footer-placeholder").load("footer.html");
}

var counter = 1;
var limit = 3;
function addInput(divName){
     if (counter == limit)  {
          alert("You have reached the limit of adding " + counter + " inputs");
     }
     else {
          var newdiv = document.createElement('div');
          newdiv.innerHTML = "Entry " + (counter + 1) + " <br><input type='datetime-local' id='time_availability' name='time_availability' min='2020-01-01T00:00' max='2020-12-31T00:00'>";
          document.getElementById(divName).appendChild(newdiv);
          counter++;
     }
}  

async function getAlert(){
     const response = await fetch('/alert');
     const text = await response.text();
     console.log(text);
     if(text.includes("1")){
        alert("You already have an interview coming up! Only can confirm one interview at a time.");
     }    
}

