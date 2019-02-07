/*global Precinct District highlightFeature*/

let greenStates = ['UT', 'NM', 'SC'];

class State {
  stateStyle() {
    return {
      fillColor: this.color, weight: 2, opacity: 1, color: 'white', dashArray: '3', fillOpacity: 0.7
    };
  }

  select() {
    this.selected = true;
  }

  unselect() {
    this.selected = false;
  }

  constructor(map, id, name, abbr, featureGeoJson) {
    this.id = id;
    this.name = name;
    this.abbr = abbr;
    if (greenStates.includes(this.abbr)) {
      this.color = 'green';
    } else {
      this.color = 'black';
    }

    this.preDistricts = {};
    this.poolDistrict = new District('-1', null, this.abbr, '');

    // properties of geojson
    this.geojson = featureGeoJson;
    this.type = 'feature';
    if (this.geojson) {
      this.properties = this.geojson.properties;
      this.geometry = this.geojson.geometry;
    }
    // external relations

    this.map = map;
    this.layer = undefined;
    this.rendered = false;
    this.selected = false;
    this.dataLoaded = false;
    this.districts = {}; // keyed by id
    this.electionData = {};

    this.bounds = undefined;
  }

  setGeoJson(feat) {
    this.geojson = feat;
    this.properties = this.geojson.properties;
    this.geometry = this.geojson.geometry;
  }

  render() {
    this.layer = this.map.renderGeoJson(this.geojson, {
      style: () => this.stateStyle(this.name), onEachFeature: (feature, layer) => layer.on({

        click: this.map.stateClicked.bind(this.map),
      })
    });
    this.layer.on('mouseover', highlightFeature);
    this.layer.on('mouseout', (e) => {
      let layer = e.target;

      layer.setStyle(this.stateStyle(this.name));
    });

    this.rendered = true;
  }

  unrender() {
    if (this.rendered) {
      this.map.removeLayer(this.layer);
      this.rendered = false;
      this.layer = undefined;
    }
  }

  showPartyColors() {
    for (let d of Object.values(this.districts)) {
      let dv = this.electionData[parseInt($('#year-select').attr('code'))][d.id].totalDemVotes;
      let rv = this.electionData[parseInt($('#year-select').attr('code'))][d.id].totalRepVotes;
      d.layer.setStyle({
        fillColor: dv > rv ? '#03d' : '#d30',
        weight: 4,
        color: '#fff',
        dashArray: '4',
        fillOpacity: 0.9
      });
      d.layer.bringToFront();
    }
  }

  saveCurrentDistricting() {
    let allPrecincts = {};
    for (let district of Object.values(this.districts)) {
      this.preDistricts[district.id] = [];
      for (let precinct of Object.values(district.precincts)) {
        this.preDistricts[district.id].push(precinct.id);
        allPrecincts[precinct.id] = precinct;
      }
    }
    return allPrecincts;
  }

  toggleDistricting() {
    let p = JSON.parse(JSON.stringify(this.preDistricts));
    console.log(p);
    for (let district of Object.values(this.districts)) {
      district.unrender();
      for (let precinct of Object.values(district.precincts)) {
        precinct.unrender();
      }
    }
    let allPrecincts = this.saveCurrentDistricting();
    for (let district_id of Object.keys(p)) {
      let ps = {};
      for (let precinct_id of p[district_id]) {
        let precinct = allPrecincts[precinct_id];
        precinct.districtId = district_id;
        precinct.color = this.districts[district_id].color;
        ps[precinct_id] = precinct;
      }
      this.districts[district_id].precincts = ps;
    }
    console.log(this.districts);
    for (let district of Object.values(this.districts)) {
      district.render(this.map);
      for (let precinct of Object.values(district.precincts)) {
        precinct.render(this.map);
        precinct.layer.bringToFront();
      }
    }
  }

  countDistrictRepresentatives() {
    let demDistricts = 0;
    let repDistricts = 0;
    console.log("COUNTING DISTRICTS");
    console.log(this.electionData);
    for (let d of Object.values(this.districts)) {
      let options = ['id', 'CD', 'CD114FP', 'CD113FP', 'CD111FP', 'CD112FP', 'CD115FP'];

      let idx = 0;
      let electionDatumElement;
      do {
        let atmpt = Number(d.properties[options[idx++]]);
        electionDatumElement = this.electionData[parseInt($('#year-select')
            .attr('code'))][atmpt];
      } while (!electionDatumElement && idx < options.length);
      let dv = electionDatumElement.totalDemVotes;
      let rv = electionDatumElement.totalRepVotes;

      console.log('Total votes: ' + electionDatumElement.totalVotes);

      console.log('District population ' + d.calculatePopulation());
      for (let p of Object.values(d.precincts)) {
        let demVotesEach = (dv / electionDatumElement.totalVotes) * p.population;
        let repVotesEach = (rv / electionDatumElement.totalVotes) * p.population;
        p.demVotes = Math.floor(demVotesEach);
        p.repVotes = Math.floor(repVotesEach);
      }

      if (dv > rv) {
        console.log("FOUND A BLUE DISTRICT");
        demDistricts = demDistricts + 1;
      } else {
        console.log("FOUND A RED DISTRICT");
        repDistricts = repDistricts + 1;
      }
    }
    $('#dem-dis-count-before').html(demDistricts);
    $('#rep-dis-count-before').html(repDistricts);
  }

  hidePartyColors() {
    for (let d of Object.values(this.districts)) {
      let dv = this.electionData[parseInt($('#year-select').attr('code'))][d.id].totalDemVotes;
      let rv = this.electionData[parseInt($('#year-select').attr('code'))][d.id].totalRepVotes;
      d.layer.setStyle({
        fillColor: dv > rv ? '#03d' : '#d30',
        weight: 1,
        color: '#ddd',
        dashArray: '4',
        fillOpacity: 0
      });
      d.layer.bringToBack();
    }
  }

  addPrecinctsFromResponse(precincts) {
    let idx = 1;
    console.log(precincts);
    for (let feature of precincts) {
      let id = feature.properties.id || ('P' + idx++);
      let area = feature.properties.shape_area;
      let p = new Precinct(id, feature, area);
      let districtId = feature.properties.containing_district_id;
      for (let district of Object.values(this.districts)) {
        if (district.id === districtId) {
          district.precincts[p.id] = p;
          p.districtId = districtId;
          p.color = district.color;
        }
      }
    }
  }

  redrawPrecinctsFromNewDistricts(oldPrecincts, precinctData) {
    let idx = 1;
    let precincts = precinctData.features;
    console.log(precincts);
    for (let feature of precincts) {
      let districtId = feature.properties.containing_district_id;
      let p = oldPrecincts[feature.properties.id];
      if (p) {
        p.unrender();

        let found = false;
        for (let district of Object.values(this.districts)) {
          if (district.id === District.validateDid(districtId)) {
            p.districtId = District.validateDid(districtId);
            p.color = district.color;
            district.precincts[p.id] = p;
            found = true;
          }
        }
        if (!found) {
          console.log('Districts ' + Object.values(this.districts).length);
          console.log('Precinct ' + p.id);
          console.log('Precinct in ' + p.containing_district_id);
        }
        p.render(map);
      }
    }
  }

  addDistrictsFromResponse(districts) {
    let idx = 1;
    let oldPrecincts = {};
    for (let district of Object.values(this.districts)) {
      for (let precinct of Object.values(district.precincts)) {
        oldPrecincts[precinct.id] = precinct;
        precinct.unrender();
      }
      district.unrender();
    }
    this.districts = {};
    districts = districts.features || districts;
    for (let feature of districts) {
      let did = District.validateDid(feature.properties.id) || ('D' + idx++);
      this.districts[did] = new District(did, feature.properties.DISTRICT, this.abbr, feature);
      // please do not do this
      // this.districts[did].render(this.map);

    }
    Map.updateMaxSeeds(Object.values(this.districts).length);
    return oldPrecincts;
  }

  showOriginalDistricts() {
    this.toggleDistricting();
    this.map.sidebar.disableSideButtons();
  }

  hideOriginalDistricts() {
    this.toggleDistricting();
    this.map.sidebar.enableSideButtons();
  }

  renderPrecincts(areaThreshold) {
    for (let district of Object.values(this.districts)) {
      for (let precinct of Object.values(district.precincts)) {
        if (precinct.area > areaThreshold) {
          precinct.render(this.map);
        }
      }
    }
  }

  renderDistricts() {
    for (let d of Object.values(this.districts)) {
      d.render(this.map);
    }
  }

  showEmptyStatePopup() {
    this.layer.bindPopup(`${this.name} has no districts! Choose another state please.`).openPopup();
  }
}
