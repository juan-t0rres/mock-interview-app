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
    const response = await fetch('/data');
    const listings = await response.json();
    let html = "";
    for(const listing of listings) {
        if(language != "None" && listing.programmingLanguage != language)
            continue;
        html += createListing(listing, false);
    }
    document.getElementById('interview-listings').innerHTML = html;
}

function createListing(listing,dashboard) {
  let html = `<div class="listing">`;
  html += `<p><b>Name:</b> ${listing.name}</p>`;
  html += `<p><b>Spoken Language:</b> ${listing.spokenLanguage}</p>`;
  html += `<p><b>Programming Language:</b> ${listing.programmingLanguage}</p>`;
  html += `<p><b>Preferred Topic (If Any):</b> ${listing.topic}</p>`;
  html += `<a class="btn btn-primary btn-sm" href="InterviewRequestDetails.html?key=${listing.key}">View Listing</a>`;
  if (dashboard) {
    html += `<form action="/cancel?key=${listing.key}" method="POST" style="float: right;">`;
    html += `<input class="btn btn-danger btn-sm" type="submit" value="Cancel" />`;
    html += `</form>`;
  }
  html += `</div>`;
  return html;
}

async function getInterviewDetails() {
    const queryString = window.location.search;
    const urlParams = new URLSearchParams(queryString);
    const key = urlParams.get('key');
    if (key == null) {
      alert('Error: No key specified.');
      return;
    }
    const response = await fetch('/data?key=' + key);
    const listing = await response.json();
    console.log(listing);
    $("#listing-name").text(listing.name);
    $("#listing-spoken").text(listing.spokenLanguage);
    $("#listing-programming").text(listing.programmingLanguage);
    $("#listing-topic").text(listing.topic);
    $("#listing-intro").text(listing.intro);

    let timesAvailable = "";
    for (const time of listing.timesAvailable)
      timesAvailable += `<p>${time}</p>`;

    $("#times-available").html(timesAvailable);

}

async function login() {
  const response = await fetch('/login');
  const loginResponse = await response.json();
  const url = loginResponse.url;

  if (loginResponse.loggedIn) {
    $("#loggedOut").hide();
    $("#loggedIn").show();
    $("#logout-button").attr("href",url);

    const userResponse = await fetch('/user');
    const userDashboard = await userResponse.json();
    console.log(userDashboard);
    const username = userDashboard.username;
    $("#username").text(username.substr(0,username.indexOf('@')));

    if (userDashboard.pending == null)
      $("#pending-listing").html("<p>You currently have no open interview requests.</p>");
    else
      $("#pending-listing").html(createListing(userDashboard.pending,true));

    if (userDashboard.past.length == 0) {
      $("#past-listings").html("<p>You have no past interview requests.</p>");
    }
    else {
      let html = "";
      for (const listing of userDashboard.past)
        html += createListing(listing,false);
      $("#past-listings").html(html);
    }
  }
  else {
    $("#loggedOut").show();
    $("#loggedIn").hide();
    $("#login-button").attr("href",url);
  }
}

async function getSections() {
  const response = await fetch('/login');
  const loginResponse = await response.json();
  if (loginResponse.loggedIn)
    getHeader();

  $("#footer-placeholder").load("footer.html");
}

async function getHeader() {
  $("#header-placeholder").load('header.html', async function(){
    let path = window.location.pathname;
    let page = path.substr(1,path.indexOf('.')-1);
    if (page == 'index' || page == '')
      $("#index-link").css('color','var(--secondary-color)');  
    
    if (page == 'interviews')
      $("#listings-link").css('color','var(--secondary-color)');  
    
    if(page == 'InterviewRequestForm')
      $("#request-link").css('color','var(--secondary-color)');  

    const response = await fetch('/alert');
    const text = await response.text();

    if(text.includes('1'))
      $("#request-link").hide();
    else
      $("#request-link").show();

  });
}

var counter = 1;
var limit = 10;
function addInput(divName){
     if (counter == limit)  {
          alert("You have reached the limit of adding " + counter + " time availability inputs");
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
        window.location.pathname = '/interviews.html';
     }    
}

