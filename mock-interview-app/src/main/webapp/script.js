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
        html += `<div class="listing">`;
        html += `<p><b>Preffered Topic (If Any):</b> ${listing.topic}</p>`;
        html += `<p><b>Preffered Spoken Language:</b> ${listing.spokenLanguage}</p>`;
        html += `<p><b>Programming Language:</b> ${listing.programmingLanguage}</p>`;
        html += `<p><b>Hangouts meeting:</b> ${listing.communicationURL}</p>`;
        html += `<p><b>Programming environment:</b> ${listing.environmentURL}</p>`;
        let daysAvailable = "";
        for(const day of listing.daysAvailable) {
            daysAvailable += day + " ";
        }
        let timesAvailable = "";
        for(const time of listing.timesAvailable) {
            timesAvailable += time + " ";
        }
        html += `<p><b>Days of the Week Availability:</b> ${daysAvailable}</p>`;
        html += `<p><b>Time Availability:</b> ${timesAvailable}</p>`;
        html += `</div>`;
    }
    document.getElementById('interview-listings').innerHTML = html;
}
