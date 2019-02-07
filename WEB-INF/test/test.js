const fs = require('fs');
const http = require('http');
const request = require('request');

let fnVals = [];
console.show = console.log;
console.log = (msg) => fs.appendFileSync('test_results.mjson', msg+'\n');

let approaches = ['AWOL', "AWLA", "RGBP", "RGUG"];

let params = {
  'compactness': 1,
  'efficiencyGap': 0,
  'partisanFairness': 0,
  'populationEquality': 0,
  'numSeedDistricts': 3,
  'approach': 'AWOL',
  'stateAbbr': 'NM',
  'year': 2017
};

function zeroParams() {
  params['compactness'] = 0;
  params['efficiencyGap'] = 0;
  params['partisanFairness'] = 0;
  params['populationEquality'] = 0
}

function done() {
  let sum = 0;
  let lowest = Infinity;
  let highest = -Infinity;
  for (let m of fnVals) {
    sum += Number(m.functionValue);
    if (m.functionValue < lowest) {
      lowest = Number(m.functionValue);
    }
    if (m.functionValue > highest) {
      highest = Number(m.functionValue);
    }
  }
  let first = fnVals[0];
  console.log(`Average value: ${sum / fnVals.length}, first: ${first.functionValue}, lowest: ${lowest}, highest: ${highest}`)
}

const testCaseTotal = 3;
let testCase = 1;
let run = 0;


test();

function test() {
    console.log(testCase);
    switch (testCase) {
      case 1:
        console.log('Only 1 metric');
        zeroParams();
        params.compactness = 1;
        console.log('Compactness=1');
        initForState('NM');
        break;
      case 2:
        zeroParams();
        params.efficiencyGap = 1;
        console.log('Efficiency gap=1');
        initForState('NM');
        break;
      case 3:
        zeroParams();
        params.populationEquality = 1;
        console.log('Population Equality=1');
        initForState('NM');
        break;
      case 4:
        console.log('Two metrics 0.5 each');
        console.log('Compactness=0.5 and efficiency gap=0.5');
        zeroParams();
        params.compactness = 0.5;
        params.efficiencyGap = 0.5;
        initForState('NM');
        break;
      case 5:
        console.log('Compactness=0.5 and population equality=0.5');
        zeroParams();
        params.compactness = 0.5;
        params.populationEquality = 0.5;
        initForState('NM');
        break;
      case 6:
        console.log('population equality=0.5 and efficiency gap=0.5');
        zeroParams();
        params.populationEquality = 0.5;
        params.efficiencyGap = 0.5;
        initForState('NM');
        break;
      case 7:
        console.log('compactness and efficiency gap');
        zeroParams();
        params.compactness = 0.5;
        params.efficiencyGap = 0.5;
        initForState('NM');
        break;
    }


}

function initForState(state) {
  loadIndex(state);
}


function loadIndex(state) {
  http.get('http://localhost:8080/CSE308', (resp) => {
    let data = '';
    resp.on('data', (chunk) => {
    });
    resp.on('end', () => loadAllStates(state));

  }).on("error", (err) => console.error("Error: " + err.message));
}

function loadAllStates(state) {
  http.get(`http://localhost:8080/CSE308/index?state=all`, (resp) => {
    let data = '';
    resp.on('data', (c) => {
    });
    resp.on('end', () => loadState(state));
  }).on("error", (err) => console.error("Error: " + err.message));
}

function loadState(stateAbbr) {
  http.get(`http://localhost:8080/CSE308/index?state=${stateAbbr}&zoom=7`, (resp) => {
    let data = '';
    resp.on('data', (chunk) => {
    });
    resp.on('end', () => sendStartRequest(stateAbbr));
  }).on("error", (err) => console.error("Error: " + err.message));
}

function sendStartRequest(state) {

  let postData = {
    'compactness': params.compactness || 0.5,
    'efficiencyGap': params.efficiencyGap || 0.5,
    'partisanFairness': params.partisanFairness || 0.5,
    'populationEquality': params.populationEquality || 0.5,
    'numSeedDistricts': params.numSeedDistricts || 3,
    'approach': params.approach || 'AWOL',
    'stateAbbr': state,
    'year': params.year || 2017
  };

  let options = {
    method: 'post', body: postData, json: true, url: 'http://localhost:8080/CSE308/start_algo'
  };

  request(options, function (err, res, body) {
    if (err) {
      console.error('error posting json: ', err);
      throw err
    }
    let headers = res.headers;
    let statusCode = res.statusCode;
    askForUpdates();

  });
}

function askForUpdates() {
  let keepGoing = true;
  let options = {
    method: 'get', url: 'http://localhost:8080/CSE308/start_algo?cont=1'
  };
  request(options, function (err, res, body) {
    if (err) {
      console.error('error posting json: ', err);
      throw err
    }
    let headers = res.headers;
    let statusCode = res.statusCode;
    // do not use ===
    if (statusCode != 200) {
      return;
    }
    let data = JSON.parse(body);
    keepGoing = data.algorithmDone === 'false';
    for (let move of data.moves) {
      fnVals.push(move);
    }
    if (keepGoing) {
      askForUpdates();
    } else {
      done();
      console.log('');
      testCase++;
      if (testCase > testCaseTotal) {
        testCase = 1;
        run++;
      }
      if (run < 4) {
        test();
      } else {
        params.approach = approaches[run];
        console.show('Running approach: ' + params.approach);
      }
    }

  });

}
