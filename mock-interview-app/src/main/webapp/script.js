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
    $("#interview-listings").html(html);
    $("#content").show();
}

function createListing(listing, dashboard, matched) {
  let html = `<div class="card listing">`;
  if (matched) {
    html += `<div class="card-header bg-success text-white">Matched!</div>`;
  }
  html += `<div class="card-body">`;
  html += `<h5 class="card-title">${listing.name}</h5>`;
  html += `<p class="card-text"><b>Spoken Language:</b> ${listing.spokenLanguage}</p>`;
  html += `<p class="card-text"><b>Programming Language:</b> ${listing.programmingLanguage}</p>`;
  html += `<p class="card-text"><b>Preffered Topic:</b> ${listing.topic}</p>`;
  html += `<a class="btn btn-primary btn-sm" href="InterviewRequestDetails.html?key=${listing.key}">View Listing</a>`;
  if (dashboard) {
    html += `<form action="/cancel?key=${listing.key}" method="POST" style="float: right;">`;
    html += `<input class="btn btn-danger btn-sm" type="submit" onclick="return confirm('Are you sure you want to cancel this mock interview?');" value="Cancel" />`;
    html += `</form>`;
  }
  html += `</div></div>`;
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
    const detailsResponse = await response.json();
    const listing = detailsResponse.interviewRequest;
    const hideForm = detailsResponse.hideForm;

    $("#listing-name").text(listing.name);
    $("#listing-spoken").text(listing.spokenLanguage);
    $("#listing-programming").text(listing.programmingLanguage);
    $("#listing-topic").text(listing.topic);
    $("#listing-intro").text(listing.intro);

    let today = new Date();
    let k = -1;
    for (const time of listing.timesAvailable) {
      k++;
      let date = new Date(time);
      // don't display options that are no longer valid
      if (date < today)
        continue;
      
      let fDate = formatDate(date) + ' (UTC)';
      let value = fDate + ';' + k;

      if (!hideForm) {
        $("#time-form").append(`<input type="checkbox" class="form-check-input" value="${value}" name="time-checkbox" />`);
        $("#time-form").append(`<label class="form-check-label" for="time-checkbox">${fDate}</label><br>`);
      }
    }

    if (!hideForm) {
      $("#time-form").append(`<br><input class="btn btn-primary btn-sm" type="submit" value="Interview This Person" />`);
      $("#time-form").attr('action','/match?key='+key);
      $("#time-form").show();
    }

    if (hideForm && listing.chosenTime != -1) {
      let chosen = new Date(listing.timesAvailable[listing.chosenTime]);
      let chosenTime = formatDate(chosen) + ' (UTC)';
      $("#time").append(`<p><b>Chosen Time:</b> ${chosenTime}</p>`);
      $("#matched").show();
    }

    // only allow one checkbox to be checked
    $('input[type="checkbox"]').on('change', function() {
      $(this).siblings('input[type="checkbox"]').prop('checked', false);
    });
}

async function login() {
  const response = await fetch('/login');
  const loginResponse = await response.json();
  const url = loginResponse.url;

  const queryString = window.location.search;
  const urlParams = new URLSearchParams(queryString);
  const matched = urlParams.get('matched');
  const cancelled = urlParams.get('cancel');

  if (loginResponse.loggedIn) {
    $("#logout-button").attr("href",url);

    // Set up dashboard
    const userResponse = await fetch('/user');
    const userDashboard = await userResponse.json();
    console.log(userDashboard);
    const username = userDashboard.username;
    $("#username").text(username.substr(0,username.indexOf('@')));
    
    if (userDashboard.pending.length == 0) {
      $("#pending-listing").html(`<p>You currently have no upcoming/pending mock interviews.</p>`);
    }
    else {
      for(const listing of userDashboard.pending)
        $("#pending-listing").append(createListing(listing,true,listing.match != null));
    }

    if (userDashboard.past.length == 0) {
      $("#past-listings").html(`<p>You have no past interview requests.</p>`);
    }
    else {
      let html = "";
      for (const listing of userDashboard.past)
        html += createListing(listing,false);
      $("#past-listings").html(html);
    }
    
    // Add alert if someone came from a match.
    if (matched != null) {
      if (matched == 'true') {
        $("#header-placeholder").append(`<div class="alert alert-success" role="alert">
          Success! An email confirmation has been sent to you and the interviewee containing the details of the interview. </div>`);
      }
      if (matched == 'false') {
        $("#header-placeholder").append(`<div class="alert alert-danger" role="alert">
          Server Error: Match unsuccessful.</div>`);
      }
    }

    // Add alert if someone came from a cancelllation
    if (cancelled != null) {
      if (cancelled == 'true') {
        $("#header-placeholder").append(`<div class="alert alert-danger" role="alert">
          You have successfully cancelled your mock interview. An email has been sent to the other user if there was a match.</div>`);
      }
    }

    $("#loggedIn").show();
  }
  else {
    $("#login-button").attr("href",url);
    $("#loggedOut").show();
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

    if(!text.includes('1'))
      $("#request-link").show();

  });
}

var counter = 1;
var limit = 10;
let today = new Date().toISOString().substr(0,16);
function addInput(divName){
     if (counter == limit)  {
          alert("You have reached the limit of adding " + counter + " time availability inputs");
     }
     else {
          var newdiv = document.createElement('div');
          newdiv.innerHTML = `Entry ${counter+1} <br><input type='datetime-local' class='form-control' id='time_availability' name='time_availability' min='${today}' required>`
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
        window.location.pathname = '/';
     }
     getMin();
}

function getMin() {
  $("#time_availability").attr('min',today);
}


