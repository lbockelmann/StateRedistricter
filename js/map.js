/*global L District initialStateZoom Precinct makeQueryURL currentState geojson stateToAbbr */

/* exports Map style highlightFeature onEachPrecinctFeature */

const INIT_PRECINCT_SIZE = 1;

class Map {
  constructor(tagID, latlng, zoom, zoomStep = 1) {
    this.tagID = tagID;
    this.latlng = latlng;
    this.zoom = zoom;
    this.sidebar = undefined;
    this.currentState = undefined;
    this.states = {};
    this.map = L.map(tagID, {
      zoomSnap: 0, zoomDelta: zoomStep
    }).setView(latlng, zoom);
    this.paused = false;
  }

  static urlForStateInnerBounds(state, zoomLevel) {
    zoomLevel = (zoomLevel > 0 ? zoomLevel : initialStateZoom[state.abbr]) || 7;
    return makeQueryURL('/CSE308/index', {
      state: state.abbr, zoom: zoomLevel
    });
  }

  pause() {
    this.paused = true;
    let $function = $('#function-button');
    $function.removeClass('btn-warning');
    $function.addClass('btn-primary');
    $function.off();
    $('#accordion').show();
    $function.click(this.unpause.bind(this));
    $function.html('Resume');
  }

  unpause() {
    this.paused = false;
    let $function = $('#function-button');
    $function.removeClass('btn-primary');
    $function.addClass('btn-warning');
    $function.off();
    $('#accordion').hide();
    $function.click(pauseAlgorithm);
    $function.html('Pause');
    askForUpdate(updateGo);
  }

  getZoom() {
    return this.map.getZoom();
  }

  getOptions() {
    return this.map.options;
  }

  removeLayer(layer) {
    this.map.removeLayer(layer);
  }

  rawMap() {
    return this.map;
  }

  addSidebar(sidebar) {
    this.sidebar = sidebar;
  }

  addTileLayer(url, options) {
    L.tileLayer(url, options).addTo(this.map);
  }

  on(eventName, callback) {
    this.map.on(eventName, callback);
  }

  resetMap(e) {
    if (e) {
      e.preventDefault();
    }

    this.unbindStatePopup();
    if (this.currentState) {
      this.currentState.unselect();
      for (let district of Object.values(this.currentState.districts)) {
        district.unrender();
        for (let precinct of Object.values(district.precincts)) {
          precinct.unrender();
        }
      }
    }
    if (this.sidebar) {
      this.sidebar.resetSidebar();
    }
    haltAlgorithmHandler(e);
    $('#show-party-colors-switch').prop('checked', false);
    $('#show-original-districts-switch').prop('checked', false);
    $("#utah-const").hide();
    $("#new-mexico-const").hide();
    $("#south-carolina-const").hide();
    this.currentState = undefined;
    this.unlockMap();
    this.setView([38.007, -95.844], 5);
  }

  setView(latlgn, zoom) {
    this.map.setView(latlgn, zoom);
  }

  lockMap() {
    this.map.setMinZoom(Math.floor(this.map.getZoom()));
  }

  unlockMap() {
    this.map.options.minZoom = 0;
  }

  fitBounds(bounds) {
    this.map.fitBounds(bounds);
    this.map.setZoom(Math.floor(this.map.getZoom()));
  }

  bindStatePopups() {
    for (let state of Object.values(this.states)) {
      if (!state.selected && state.rendered) {
        state.layer.bindPopup('Reset the map before choosing another state.<br/> You will lose current unsaved changes.');
        state.layer.closePopup();
      }
    }
  }

  unbindStatePopup() {
    console.log(this.states);
    for (let state of Object.values(this.states)) {
      if (!state.selected && state.rendered) {
        state.layer.closePopup();
        state.layer.unbindPopup();
      }
    }
  }

  renderGeoJson(gjObject, options) {
    let g = L.geoJson(gjObject, options);
    g.addTo(this.map);
    return g;
  }

  static updateMaxSeeds(n) {
    $('#nDistricts').attr('placeholder', `Current: ${n}`);
  }

  selectState(stateAbbreviation, bounds) {
    if (this.currentState && this.currentState.abbr !== stateAbbreviation) {
      this.bindStatePopups();
    } else if (this.currentState) {
      this.unbindStatePopup();
    } else {
      this.unbindStatePopup();
      this.loadMapState(stateAbbreviation, bounds);
    }
  }

  loadMapState(abbr, bounds) {
    console.log(abbr);
    console.log(event.target);
    this.currentState = this.states[abbr];
    this.currentState.select();
    if (!this.currentState.dataLoaded) {
      this.loadStateInnerBounds();
    } else {
      Map.updateMaxSeeds(Object.values(this.currentState.districts).length);
      this.renderDistricts();
      this.renderPrecincts(INIT_PRECINCT_SIZE);
      this.showStateElectionData();
    }
    this.currentState.bounds = bounds;
    this.fitBounds(bounds);
    this.sidebar.enableSideButtons(event);
    if(abbr === "UT"){
      $("#utah-const").show();
    }
    else if(abbr === "NM"){
      $("#new-mexico-const").show();
    }
    else{
      $("#south-carolina-const").show();
    }
    this.lockMap();
  }

  showStateElectionData() {
    let dTotal = 0, rTotal = 0;
    for (let d of Object.values(this.currentState.districts)) {
      let dv = this.currentState.electionData[parseInt($('#year-select').attr('code'))][d.id].totalDemVotes;
      let rv = this.currentState.electionData[parseInt($('#year-select').attr('code'))][d.id].totalRepVotes;
      console.log(dv);
      console.log(rv);
      dTotal += dv;
      rTotal += rv;
    }
    $('#dem-dis-count-before').html(`${dTotal}`);
    $('#rep-dis-count-before').html(`${rTotal}`);
  }

  stateClicked(event) {
    let stateName = event.target.feature.properties.NAME;
    let abbr = stateToAbbr[stateName];
    console.log(this.currentState);
    console.log(abbr);
    moveData = {
      performed: 0, undone: 0, previousOrigin: null, previousDestination: null, previousPid: null, firstValue: null, currentValue: null, hasFirst: false
    };
    this.selectState(abbr, event.target.getBounds());

  }

  loadStateElectionData() {
    let that = this;
    let req = new XMLHttpRequest();
    let reqUrl = Map.urlForStateElectionData(this.currentState);
    req.onreadystatechange = function () {
      if (req.status === 200 && req.readyState === 4) {
        that.currentState.electionData = JSON.parse(req.response);
        console.log('Showing election data');
        that.currentState.countDistrictRepresentatives();
        // that.showStateElectionData();
      }
    };
    req.open('GET', reqUrl);
    req.send();
  }

  loadStateInnerBounds() {
    let that = this;
    let req = new XMLHttpRequest();
    let reqUrl = Map.urlForStateInnerBounds(this.currentState, -1);
    console.log('Fetching ' + this.currentState);
    console.log(reqUrl);
    req.open('GET', reqUrl, true);
    req.onreadystatechange = function (/*event*/) {
      if (req.status === 200 && req.readyState === 4) {
        console.log(req.response);
        let b = JSON.parse(req.response);
        let districts = b.districts.features || b.districts;
        let precincts = b.precincts.features || b.precincts;
        console.log(b);
        console.log(districts);
        console.log(precincts);

        if (!districts || !precincts) {
          that.currentState.showEmptyStatePopup();
        } else {
          that.currentState.addDistrictsFromResponse(districts);
          that.currentState.addPrecinctsFromResponse(precincts);
          that.renderDistricts();
          that.renderPrecincts(INIT_PRECINCT_SIZE);
          that.currentState.dataLoaded = true;

          that.loadStateElectionData();
        }

      }
    };
    req.setRequestHeader('Content-Type', 'text/plain');
    req.send();
  }

  renderPrecincts(areaThreshold) {
    this.currentState.renderPrecincts(areaThreshold);
  }

  renderDistricts() {
    this.currentState.renderDistricts();
  }

  static urlForStateElectionData(currentState) {
    return makeQueryURL('/CSE308/election_data', {
      state: currentState.abbr
    });
  }
}

function precinctBorderDistrictColors(map) {
  map.precincts.filter((i) => i.district).forEach((i) => {
    i.layer.setStyle({
      color: i.district.color, opacity: 1
    });
  });
}
