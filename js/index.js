/* global stateToAbbr State usaGeoJson L Sidebar Map style highlightFeature */
/* exports makeQueryURL geojson */
// ms
const map = new Map('mapid', [38.007, -95.844], 5, 0.5);

// console.show = console.log;
// console.log = (a) => undefined;

function accountAlert(msg) {

}

let moveData = {
  performed: 0,
  undone: 0,
  previousOrigin: null,
  previousDestination: null,
  previousPid: null,
  firstValue: null,
  currentValue: null,
  hasFirst: false
};

map.addTileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}', {
  attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
  maxZoom: 18,
  id: 'mapbox.light',
  accessToken: 'pk.eyJ1IjoiaWphc3dhbCIsImEiOiJjam0ya3Q2dGQyemY2M2tvZ2txbmFicGJkIn0.Ivh3AaGv_Hll_laTT8KBYA',
});

(function loadStateData() {
  let req = new XMLHttpRequest();
  req.onreadystatechange = () => {
    if (req.readyState === 4) {
      console.log(req.response);
      let j = JSON.parse(req.response);
      for (let state of j.states) {
        map.states[state.abbr] = new State(map, state.id, state.name, state.abbr, null);
      }

      let stateFeatures = usaGeoJson.features;
      console.log(stateFeatures);
      for (let stateFeature of stateFeatures) {
        let state = map.states[stateToAbbr[stateFeature.properties['NAME']]];
        if (state) {
          state.setGeoJson(stateFeature);
          state.render(map);
        } else {
          // This is needed because the master database does not currently have
          // every state in it
          let stateName = stateFeature.properties['NAME'];
          let stateAbbr = stateToAbbr[stateName];
          let state = new State(map, 10, stateName, stateAbbr, stateFeature);
          map.states[stateAbbr] = state;
          state.render();
        }
      }
    }
  };
  req.open('GET', makeQueryURL('index', {
    state: 'all'
  }));
  req.send();
})();

const sidebar = new Sidebar(map, 'sidebar');

$(document).ready(function () {
  $('#function-button').click(startAlgorithmHandler);
  $('#halt-button').click(haltAlgorithmHandler);

  $('#show-other-accounts-button').click(showOtherAccountsHandler);

  $("#popup-info").hide();
  $("#utah-const").hide();
  $("#new-mexico-const").hide();
  $("#south-carolina-const").hide();

  $('#accountButton').hide();
  $('#accordion2').hide();
  $('#login-checker').hide();
  $('#UserResponse').hide();
  $('#WeightResponse').hide()
  $("#message-close").click((event) => hideMessage(event));
  $("#weight-message-close").click((event) => hideWeightMessage(event));

  $('#zoomButton').click((event) => map.resetMap(event));
  $('#loginButton').click((event) => startSession(event));
  $('#userLoginSubmit').click((event) => submitLogin(event, 'true'));
  $('#userCreateButton').click((event) => submitLogin(event, 'false'));

  $('#adminAddButton').click((event) => addAccount(event));
  $('#adminDeleteButton').click((event) => deleteAccount(event));

  $('#saveweights-button').click((event) => saveWeights(event));
  $('#mapSaveButton').click((event) => saveMap(event));
  $('#loadweights-button').click((event) => loadWeights(event));

  $('#restore-districts-button').click(restoreDistricts);
  $('#mapDeleteButton').click((event) => deleteMap(event));

  $('#loginModal').keypress((event) => {
    if (event.keyCode === 13) {
      $('#userLoginSubmit').click();
    }
  });

  $('#show-party-colors-switch').change(function (event) {
    if ($(this).is(':checked')) {
      map.currentState.showPartyColors();
    } else {
      map.currentState.hidePartyColors();
    }
  });

  $('#show-original-districts-switch').change(function (event) {
    if ($(this).is(':checked')) {
      map.currentState.showOriginalDistricts();
    } else {
      map.currentState.hideOriginalDistricts();
    }
  });

  sidebar.addExistingButtonID('#homeButton');
  sidebar.addExistingButtonID('#functionButton');
  sidebar.addExistingButtonID('#restore-districts-button');
  //sidebar.addExistingButtonID('#saveweightsButton');
  sidebar.addExistingButtonID('#constitutionButton');
  sidebar.functionButton = $('#function-button');
  sidebar.randomizeButton = $('#randomize-button');
  //sidebar.saveweightsButton = $('#saveweights-button');

  sidebar.on('opening', function (event) {
    sidebar.showing = true;
  });
  sidebar.on('closing', function (event) {
    sidebar.showing = false;
  });

  sidebar.addExistingSliderID('#slider1');
  sidebar.addExistingSliderID('#slider2');
  sidebar.addExistingSliderID('#slider3');
  sidebar.addExistingSliderID('#slider4');
  sidebar.addExistingSliderID('#slider5');


  $('.algo-select-dropdown-item').click(selectAlgorithmClicked);

  $('#dem-vote-count-before').css('color', 'blue');
  $('#dem-dis-count-before').css('color', 'blue');
  $('#dem-dis-count-after').css('color', 'blue');
  $('#rep-vote-count-before').css('color', 'red');
  $('#rep-dis-count-before').css('color', 'red');
  $('#rep-dis-count-after').css('color', 'red');


  $('.year-select-dropdown-item').click(yearSelectClicked);

  // $('#map-load').click((event) => getSavedMaps(event));
  //
  $('#search-submit-button').click(function (event) {
    event.preventDefault();
    doSearch();
  });
  $('#search-field').keypress(function (event) {
    if (event.which === 13) {
      event.preventDefault();
      $('#search-submit-button').click();
    }
  });
});

function selectAlgorithmClicked(event) {
  let $algo = $('#algo-select');
  $algo.html($(this).html());
  $algo.attr('code', $(this).attr('code'));
}

function yearSelectClicked(event) {
  let $year = $('#year-select');
  $year.html($(this).html());
  $year.attr('code', $(this).attr('code'));
  if (map.currentState) {
    if ($('#show-party-colors-switch').prop('checked')) {
      map.currentState.hidePartyColors();
      map.currentState.showPartyColors();
    }
  }
  let req = new XMLHttpRequest();
  req.onreadystatechange = () => {
    if (req.status === 200 && req.readyState === 4) {
      console.log(req.response);
      let resp = JSON.parse(req.response);
      let districts = resp.districts;
      let precinctsData = resp.precincts;
      let oldPrecincts = map.currentState.addDistrictsFromResponse(districts);
      map.currentState.redrawPrecinctsFromNewDistricts(oldPrecincts, precinctsData);
      map.currentState.countDistrictRepresentatives();
    }
  };
  req.open('GET', makeQueryURL('index', {
    state: map.currentState.abbr, year: $(this).attr('code')
  }));
  req.send();
}


function updateLoadSliders(arr) {
  console.log(arr);
  for (n = 1; n < 5; n++) {
    $('#slider' + n).val(arr[n]).trigger('input');
  }
  $('#nDistricts').val(arr[5]);
  $('#algo-' + arr[6]).trigger('click');
  // $('#year-' + arr[7]).trigger('click');
}


// used to decide when to stop updating the map
let halted = false;

function doSearch() {
  let query = $('#search-field').val();
  if (query.indexOf(',') >= 0) {
    let pieces = query.split(',');
    if (pieces.length === 2) {
      let lat = parseFloat(pieces[0]);
      let lng = parseFloat(pieces[1]);
      map.setView([lat, lng], 8);
      displayResults(query, [lat, lng]);
      return;
    }
  } else {
    for (let state of Object.values(map.states)) {
      if (state.name.toLowerCase() === query.toLowerCase().trim()) {
        map.fitBounds(state.layer.getBounds());
        displayResults(query, state.name);
        return;
      }
    }
  }
  displayResults(query, null);
}

function displayResults(query, result) {
  let resultsField = $('#search-results');
  resultsField.css('font-weight', 'bold');
  if (!result) {
    resultsField.css('color', 'red');
    resultsField.html('No results found for: ' + query);
  } else {
    resultsField.css('color', 'green');
    resultsField.html('Found result for ' + query + ': ' + result);
  }
}

function restoreDistricts() {
  map.currentState.dataLoaded = false;
  let x = new XMLHttpRequest();
  x.onreadystatechange = function () {
    if (x.readyState === 4 && x.status === 200) {

      map.loadMapState(map.currentState.abbr, map.currentState.bounds);
    }
  };
  x.open('GET', '/CSE308/start_algo?reset=true');
  x.send();
}

function getListedAccountsInfo() {
  return {
    'username': 'dummy', 'password': 'dummy', 'isLogin': 'getAccounts'
  };
}

function getAdminNewAccountInfo() {
  return {
    'username': $('#adminUsername').val(), 'password': 'password', 'isLogin': 'adminNewAccount'
  };
}

function getDeleteAccountInfo() {
  return {
    'username': $('#adminUsername').val(), 'password': 'password', 'isLogin': 'deleteAccount'
  };
}

function getLoadWeightsInfo() {
  return {
    'username': localStorage['user'], 'password': 'dummy', 'isLogin': 'loadWeights'
  }
}

function getParamsFromDOM() {
  return {
    'compactness': $('#slider1').val(),
    'efficiencyGap': $('#slider2').val(),
    'partisanFairness': 0,
    'populationEquality': $('#slider3').val(),
    'numSeedDistricts': $('#nDistricts').val() || Object.values(map.currentState.districts).length,
    'approach': $('#algo-select').attr('code'),
    'stateAbbr': map.currentState.abbr,
    'year': $('#year-select').attr('code')
  };
}

function getFormattedWeights() {
  return {
    'username': localStorage['user'] + ',' + $('#slider1').val() + ',' + $('#slider2')
        .val() + ',' + $('#slider3').val() + ',' + 0 + ',' + ($('#nDistricts')
        .val() || Object.values(map.currentState.districts).length) + ',' + $('#algo-select')
        .attr('code') + ',' + $('#year-select').attr('code'),
    'password': 'password',
    'isLogin': 'saveWeights'
  };
}

function getAccountInfo() {
  return {
    'username': $('#userLoginUsername').val(),
    'password': $('#userLoginPassword').val(),
    'isLogin': 'false'
  };
}

function getLoginInfo() {
  return {
    'username': $('#userLoginUsername').val(),
    'password': $('#userLoginPassword').val(),
    'isLogin': 'true'
  };
}

function startSession(event) {
  event.preventDefault();
  $('#loginModal').modal();
  $('#userLoginUsername').focus();
}

const logoutClass = 'fa fa-sign-out';
const loginClass = 'fa fa-user-plus';

function submitLogin(event, isLogin) {
  event.preventDefault();
  let username = $('#userLoginUsername').val();
  let password = $('#userLoginPassword').val();
  console.log('validating login');
  let req = new XMLHttpRequest();
  req.onreadystatechange = function (/*event*/) {
    if (req.readyState === 4) {
      console.log('Login response:');
      console.log(req.response);
      if (req.status === 200) {
        let status = req.responseText;
        console.log(status);
        console.log(req.status);
        if (status === 'validlogin') {
          $('#userLoginInvalid').text('');
          $('#loginModal').modal('hide');
          $('#loginButton').off();
          $('#loginButton').click((e) => e.preventDefault());
          $('#userLogoutButton').click(handleLogout);
          localStorage['user'] = username;
          if (username === 'admin') {
            $('#show-other-accounts-button').attr('hidden', false);
            $('#adminActions').attr('hidden', false);
          }
          getSavedMaps(event);
          $('#usernameDisplay').html(`<span class="indented-text">${localStorage['user']}</span>`)
          $('#accountButton').show();
          $('#accordion2').show();
          $('#login-checker').show();
          $('#loginButton').hide();
        }
        if (status === 'validcreation') {
          alert('Account created!');
        } else if (status === 'invalidlogin') {
          alert('Invalid username or password!');
        } else if (status === 'invalidcreation' && username === localStorage['user']) {
          alert('Username is taken!');
        }
      } else {
        alert('Server error. Sorry for the inconvenience');
      }
    }
  };
  req.open('POST', '/CSE308/login', true);
  req.setRequestHeader('Content-Type', 'application/json');
  if (isLogin === 'true') {
    let loginInfo = getLoginInfo();
    console.log('json' + JSON.stringify(loginInfo));
    req.send(JSON.stringify(loginInfo));
    console.log('Sent login request with login info: ');
    console.log(loginInfo);
  } else {
    let accountInfo = getAccountInfo();
    console.log('json' + JSON.stringify(accountInfo));
    req.send(JSON.stringify(accountInfo));
    console.log('Sent login request with login info: ');
    console.log(accountInfo);
  }
}

// function toggleUserSidebar(event) {
//   event.preventDefault();
//   console.log(sidebar.showing);
//   if (!sidebar.showing) {
//     $('#usernameDisplay').text(localStorage['user']);
//     if (localStorage['user'] != 'admin') {
//       $('#usernameDisplay1').hide();
//       $('#usernameDisplay2').hide();
//       $('#usernameDisplay3').hide();
//       $('#usernameDisplay4').hide();
//       $('#usernameDisplay5').hide();
//       $('#adminAddButton').hide();
//       $('#adminDeleteButton').hide();
//       $('#adminUsername').hide();
//     }
//     sidebar.show('user-information');
//   } else {
//     sidebar.hide();
//   }
// }

function handleLogout(event) {
  delete localStorage['user'];
  sidebar.hide();
  $('#accountButton').hide();
  $('#accordion2').hide();
  $('#loginButton').show();
  window.location.reload();
}


/**
 * Used to update the map based on the move described
 * @param {JSON} move the move object that describes a change to the map
 */
function displayMove(move) {
  console.log(move);
  let originalDistrict = map.currentState.districts[move.originalDistrict];
  let destinationDistrict = map.currentState.districts[move.destinationDistrict];
  let precinct = originalDistrict.precincts[move.precinct];

  if (destinationDistrict.id === moveData.previousOrigin && originalDistrict.id === moveData.previousDestination && precinct.id === moveData.previousPid) {
    moveData.undone++;
  } else {
    moveData.performed++;
  }
  moveData.currentValue = move.functionValue;
  if (!moveData.hasFirst) {
    moveData.firstValue = move.functionValue;
    moveData.hasFirst = true;
  }

  moveData.previousOrigin = originalDistrict.id;
  moveData.previousDestination = destinationDistrict.id;
  moveData.previousPid = precinct.id;

  sidebar.showRedistrictingStatistics(moveData);

  if (!precinct) {
    console.error("UNDEFINED PRECINCT IN MOVE");
    console.table(move);
    return;
  }
  precinct.unrender();
  precinct.color = destinationDistrict.color;
  precinct.districtId = destinationDistrict.id;
  destinationDistrict.precincts[move.precinct] = precinct;
  delete originalDistrict.precincts[move.precinct];
  precinct.render(map);
}

function pauseAlgorithm(/*event*/) {
  map.pause();
}

function makeQueryURL(baseUrl, params) {
  let query = '';
  for (let key of Object.keys(params)) {
    query += (`${key}=${encodeURIComponent(params[key])}`);
    query += '&';
  }
  query = query.substring(0, query.length - 1);
  return baseUrl + '?' + query;
}

function sendStartRequest() {
  let req = new XMLHttpRequest();
  req.onreadystatechange = function () {
    if (req.readyState === 4) {
      console.log('Start algorithm response:');
      console.log(req.response);
      if (req.status === 200) {
        let approach = getParamsFromDOM().approach;
        if (approach === "RGBP" || approach === "RGUG") {
          let r = new XMLHttpRequest();
          r.onreadystatechange = function () {
            if (r.readyState === 4 && r.status === 200) {
              map.currentState.districts[-1] = map.currentState.poolDistrict;
              let seeds = r.response.split(',');
              for (let d of Object.values(map.currentState.districts)) {
                for (let p of Object.values(d.precincts)) {
                  for (let seedId of seeds) {
                    if (p.id != seedId) {
                      map.currentState.poolDistrict.precincts[p.id] = p;
                      p.districtId = -1;
                      p.color = 'green';
                    }
                  }
                }
              }
              for (let d of Object.values(map.currentState.districts)) {
                for (let p of Object.values(d.precincts)) {
                  p.unrender();
                  p.render(map);
                }
              }
              askForUpdate(updateGo);
            }
          };
          r.open('GET', makeQueryURL('/CSE308/start_algo', {'getSeedIds': true}));
          r.send();
        } else {
          askForUpdate(updateGo);
        }
      } else {
        alert('Algorithm failed to start');
      }
    }
  };
  req.open('POST', '/CSE308/start_algo', true);
  req.setRequestHeader('Content-Type', 'application/json');
  let params = getParamsFromDOM();
  console.log(params);
  req.send(JSON.stringify(params));
  console.log('Sent Algorithm start request with params: ');
  console.log(params);
}

const updateGo = makeQueryURL('/CSE308/start_algo', {
  'cont': 1
});
const updateStop = makeQueryURL('/CSE308/start_algo', {
  'cont': 0
});

function askForUpdate(url, last) {
  let req = new XMLHttpRequest();
  req.open('GET', url, true);
  req.onreadystatechange = function () {
    if (req.status === 200 && req.readyState === 4) {
      let response = JSON.parse(req.response);
      let algorithmDone = response.algorithmDone === 'true';
      let moves = response.moves;
      // console.table(moves);
      console.log('Moves this round: ' + moves.length);
      for (let move of moves) {
        displayMove(move);
      }
      console.log(algorithmDone);
      if (algorithmDone || halted) {
        console.log('Turning on controls');
        sidebar.turnStartOn();
        sidebar.enableRandomize();
        sidebar.enableUserActions();
        sidebar.enableSliders();
        $('#halt-button').attr('hidden', true);
      } else {
        console.log('Asking for next update');
        if (!map.paused) {
          askForUpdate(updateGo);
        }
      }
    } else if (req.readyState === 4) {
      console.log('Error status returned is ' + req.status);
    } else {
      console.log('Update request readyState: ' + req.readyState);
    }
  };
  req.setRequestHeader('Content-Type', 'text/plain');
  req.send();
}

function startAlgorithmHandler(event) {
  event.preventDefault();
  $('#halt-button').attr('hidden', false);
  $('#show-party-colors-switch').attr('hidden', true);
  // $('#restore-districts-button').attr('hidden', false);
  map.currentState.saveCurrentDistricting();
  sidebar.turnStartOff();
  sidebar.disableRandomize();
  sidebar.disableUserActions();
  sidebar.disableSliders();
  halted = false;
  sendStartRequest();
}

function haltAlgorithmHandler(event) {
  event.preventDefault();
  halted = true;
  $('#halt-button').attr('hidden', true);
  sidebar.turnStartOn();
  sidebar.enableRandomize();
  sidebar.enableUserActions();
  sidebar.enableSliders();
  sendHaltRequest(updateStop);
  map.unpause();
}

function sendHaltRequest(url) {
  let req = new XMLHttpRequest();
  req.open('GET', url, true);
  req.onreadystatechange = function () {
    console.log(req.status);
    if (req.status === 200 && req.readyState === 4) {
      console.log('Algorithm halted');
      console.log(JSON.parse(req.response));
    }
  };
  req.setRequestHeader('Content-Type', 'text/plain');
  req.send();
}

function highlightFeature(e) {
  let layer = e.target;
  layer.setStyle({
    weight: 5, color: 'gold', dashArray: '', fillOpacity: 0.7,
  });
}
