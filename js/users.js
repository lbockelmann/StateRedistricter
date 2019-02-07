function showOtherAccountsHandler(event) {
  if (event) {
    event.preventDefault();
  }
  let x = new XMLHttpRequest();
  x.onreadystatechange = function () {
    if (x.status === 200 && x.readyState === 4) {
      let $show = $('#show-other-accounts-button');
      $show.html('Hide Accounts');
      $show.off();
      $show.click(hideOtherAccountsHandler);
      let response = x.response;
      let lines = response.split('\n');
      console.log(lines);
      let idx = 1;
      $('#accountsList').html('');
      for (line of lines) {
        if (line) {
          $('#accountsList').append(`<div class="undisplay">${idx}. ${line}</div>`);
          idx++;
        }
      }
    }
  };

  x.open('POST', '/CSE308/login');
  x.send(JSON.stringify({isLogin: 'getAccounts'}));
}

function hideOtherAccountsHandler(event) {
  console.log("Hiding accounts");
  event.preventDefault();
  $('#accountsList').html('');
  let $show = $('#show-other-accounts-button');
  $show.html('Show Accounts');
  $show.off();
  $show.click(showOtherAccountsHandler);
}


function addAccount(event) {
  console.log('adding account');
  let req = new XMLHttpRequest();
  req.onreadystatechange = function (/*event*/) {
    if (req.readyState === 4) {
      console.log('Creation response:');
      console.log(req.response);
      if (req.status === 200) {
        let status = req.responseText;
        console.log(status);
        console.log(req.status);
        if (status === 'validcreation') {
          $('#user-message').html('Success: account created!');
          $('#UserResponse').css('background-color', 'green');
          $('#UserResponse').show();
          showOtherAccountsHandler(event);
          $('#adminUsername').val('')
        } else if (status === 'invalidcreation') {
          $('#UserResponse').css('background-color', 'red');
          $('#user-message').html('Username is taken!');
          $('#UserResponse').show();
        }
      } else {
        alert('Algorithm failed to start');
      }
    }
  };
  req.open('POST', '/CSE308/login', true);
  req.setRequestHeader('Content-Type', 'application/json');

  let adminaccountInfo = getAdminNewAccountInfo();
  console.log('json' + JSON.stringify(adminaccountInfo));
  req.send(JSON.stringify(adminaccountInfo));
  console.log('Sent creation request with login info: ');
  console.log(adminaccountInfo);

}

function stringToArray(status) {
  var arr = new Array("", "", "", "", "");
  var pos = 0;
  for (n = 0; n < status.length; n++) {
    if (status[n] == ',') {
      pos++;
      continue;
    }
    if (status[n] != ' ' && status[n] != ',' && status[n] != '[' && status[n] != ']') {
      arr[pos] = arr[pos] + status[n];
    }
  }
  return arr;
}

function hideMessage() {
  $('#UserResponse').hide();
}

function hideWeightMessage() {
  $('#WeightResponse').hide();
}

function deleteAccount(event) {
  console.log('deleting account');
  let req = new XMLHttpRequest();
  req.onreadystatechange = function (/*event*/) {
    if (req.readyState === 4) {
      console.log('deletion response:');
      console.log(req.response);
      if (req.status === 200) {
        let status = req.responseText;
        console.log(status);
        console.log(req.status);
        if (status == 'validdeletion') {
          $('#user-message').html('Success: account deleted!');
          $('#UserResponse').css('background-color', 'green');
          $('#UserResponse').show();
          showOtherAccountsHandler(event);
          $('#adminUsername').val('')
        } else if (status == 'invaliddeletion') {
          $('#user-message').html('Account does not exist!');
          $('#UserResponse').css('background-color', 'red');
          $('#UserResponse').show();
        }
      } else {
        alert('Algorithm failed to start');
      }
    }
  };
  req.open('POST', '/CSE308/login', true);
  req.setRequestHeader('Content-Type', 'application/json');

  let deleteaccountInfo = getDeleteAccountInfo();
  console.log('json' + JSON.stringify(deleteaccountInfo));
  req.send(JSON.stringify(deleteaccountInfo));
  console.log('Sent deletion request with login info: ');
  console.log(deleteaccountInfo);
}

function saveWeights(event) {
  console.log('saving weights');
  let req = new XMLHttpRequest();
  req.onreadystatechange = function (/*event*/) {
    if (req.readyState === 4) {
      console.log('save response:');
      console.log(req.response);
      if (req.status === 200) {
        let status = req.responseText;
        console.log(status);
        console.log(req.status);
        if (status == 'validsave') {
          $('#weight-message').html('Preferences saved!');
          $('#WeightResponse').css('background-color', 'green');
          $('#WeightResponse').show();
        } else if (status == 'invalidsave') {
          $('#weight-message').html('Error while saving!');
          $('#WeightResponse').css('background-color', 'red');
          $('#WeightResponse').show();
        }
      } else {
        alert('Algorithm failed to start');
      }
    }
  };
  req.open('POST', '/CSE308/login', true);
  req.setRequestHeader('Content-Type', 'application/json');

  let weightsInfo = getFormattedWeights();
  console.log('json' + JSON.stringify(weightsInfo));
  req.send(JSON.stringify(weightsInfo));
  console.log('Sent save request with login info: ');
  console.log(weightsInfo);
}

function saveMap(event) {
  console.log('saving map');
  let req = new XMLHttpRequest();
  req.onreadystatechange = function (/*event*/) {
    if (req.readyState === 4) {
      console.log('save response:');
      console.log(req.response);
      if (req.status === 200) {
        let status = req.responseText;
        console.log(status);
        console.log(req.status);
        getSavedMaps(event);
        if (status == 'validsave') {
          alert('Map saved!');
        } else if (status == 'invalidsave') {
          alert('Error while saving!');
        }
      } else {
        alert('Algorithm failed to start');
      }
    }
  };
  req.open('POST', '/CSE308/login', true);
  req.setRequestHeader('Content-Type', 'application/json');
  console.log({username: localStorage['user'], isLogin: 'savemap'})
  req.send(JSON.stringify({username: localStorage['user'], isLogin: 'savemap'}));
}

function deleteMap(event) {
  console.log('deleting map');
  let req = new XMLHttpRequest();
  req.onreadystatechange = function (/*event*/) {
    if (req.readyState === 4) {
      console.log('deletion response:');
      console.log(req.response);
      if (req.status === 200) {
        let status = req.responseText;
        console.log(status);
        console.log(req.status);
        getSavedMaps(event);
        if (status == 'validdeletion') {
        } else if (status == 'invaliddeletion') {
          alert('invalid deletion');
        }
      } else {
        alert('Algorithm failed to start');
      }
    }
  };
  req.open('POST', '/CSE308/login', true);
  req.setRequestHeader('Content-Type', 'application/json');
  let code = $('#map-load').attr('code');
  console.log(code);
  if (code === 'none') {
    return ;
  }
  req.send(JSON.stringify({username: localStorage['user'] + ',' + code, isLogin: 'deleteMap'}));
}

function getSavedMaps(event) {
  console.log('getting saved maps');
  let req = new XMLHttpRequest();
  req.onreadystatechange = function (/*event*/) {
    if (req.readyState === 4) {
      console.log('get saved maps response:');
      console.log(req.response);
      if (req.status === 200) {
        let status = req.responseText;
        console.log(status);
        console.log(req.status);
        showSavedMaps(status);
      }
    }
  };
  req.open('POST', '/CSE308/login', true);
  req.setRequestHeader('Content-Type', 'application/json');
  console.log({username: localStorage['user'], isLogin: 'getSavedMaps'});
  req.send(JSON.stringify({username: localStorage['user'], isLogin: 'getSavedMaps'}));
}

function showSavedMaps(num) {

  document.getElementById('map-load').innerText = '';
  document.getElementById('map-load').setAttribute('code', 'none');
  let myNode = document.getElementById("map-load-dropdown-list");
  while (myNode.firstChild) {
    myNode.removeChild(myNode.firstChild);
  }

  let saves = num.split(',');


  for (let save of saves) {
    if(!save){
      break;
    }
    let dropdownItem = 'Save ' + save;
    let button = document.createElement('BUTTON');
    button.setAttribute('code', '' + save);
    button.type = 'button';
    button.classList.add('map-load-dropdown-item');
    button.classList.add('dropdown-item');
    button.id = 'mapLoadButton' + save;
    button.innerText = dropdownItem;
    button.onclick = function() {
      let $sel = $('#map-load');
      $sel.text(document.getElementById('mapLoadButton' + save).innerText);
      let code = document.getElementById('mapLoadButton' + save).getAttribute('code');
      console.log(code);
      $sel.attr('code', code);
    };
    document.getElementById('map-load-dropdown-list').appendChild(button);
  }
}

function loadWeights(event) {
  console.log('loading weights');
  let req = new XMLHttpRequest();
  req.onreadystatechange = function (/*event*/) {
    if (req.readyState === 4) {
      console.log('load response:');
      console.log(req.response);
      if (req.status === 200) {
        let status = req.responseText;
        console.log(status);
        console.log(req.status);
        console.log(status);
        let arr = status.split(',');
        if (arr[0] == 'validload') {
          console.log(arr);
          updateLoadSliders(arr);
          $('#weight-message').html('Preferences loaded!');
          $('#WeightResponse').css('background-color', 'green');
          $('#WeightResponse').show();
        } else {
          $('#weight-message').html('No saved preferences exist!');
          $('#WeightResponse').css('background-color', 'red');
          $('#WeightResponse').show();
        }
      } else {
        alert('Algorithm failed to start');
      }
    }
  };
  req.open('POST', '/CSE308/login', true);
  req.setRequestHeader('Content-Type', 'application/json');

  let weightsInfo = getLoadWeightsInfo();
  console.log('json' + JSON.stringify(weightsInfo));
  req.send(JSON.stringify(weightsInfo));
  console.log('Sent load request with info: ');
  console.log(weightsInfo);
}
