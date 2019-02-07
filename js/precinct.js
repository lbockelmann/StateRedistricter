/*global highlightFeature*/
class Precinct {
  constructor(id, featureGeoJson, area) {
    this.id = id;
    this.geojson = featureGeoJson;
    this.type = 'feature';
    this.properties = this.geojson.properties;
    this.geometry = this.geojson.geometry;
    this.area = area || Infinity;

    console.log(this.properties);

    this.population = this.properties.population;

    this.map = undefined;
    this.layer = undefined;
    this.rendered = false;
    this.districtId = undefined;

    this.demVotes = undefined;
    this.repVotes = undefined;
  }

  render(map) {

    this.map = map;
    let that = this;
    this.layer = map.renderGeoJson(this.geojson, {
      style: this.getPrecinctStyles.bind(this),
      onEachFeature: function onEachPrecinctFeature(_, layer) {
        layer.on({
          mouseover: () => {
            let containingDistrictId = that.districtId;
            $("#popup-info").show();
            let demv = that.demVotes || "???";
            let repv = that.repVotes || "???";
            if (that.districtId == -1) {
              demv = '???';
              repv = '???';
            }
            document.getElementById("popup-info").innerHTML = `ID: <span class="box-ids">${that.id}</span><br/>In district: ${containingDistrictId}<br/><br/>Population: <span class="box-pop">~${that.population}</span><br/>Dem. votes: <span id="box-dem-votes">~${demv}</span><br/>Rep. Votes: <span id="box-rep-votes">~${repv}</span>`;

          }, mouseout: () => {
            $("#popup-info").hide();
          },
        });
      }
    });
    let d = this.map.currentState.districts[this.districtId];
    this.layer.on('mouseover', (e) => {
      try {
        d.addDataToSidebar();
      } catch {
        // console.warn('No District');
      }
      highlightFeature(e);
    });
    this.layer.on('mouseout', (e) => {
      try {
        d.removeDataFromSidebar();
      } catch {
        // console.warn('No District');
      }
      let layer = e.target;
      layer.setStyle(this.getPrecinctStyles());
    });

    this.rendered = true;
  }

  getPrecinctStyles() {
    return {
      fillColor: this.color, weight: 1, color: '#fff', dashArray: '3', fillOpacity: 0.9
    };
  }

  unrender() {
    if (this.rendered) {
      this.map.removeLayer(this.layer);
      this.rendered = false;
      this.layer = undefined;
    }
  }
}

function pad(s, v, n) {
  let size = s.length;
  let difference = n - size;
  let result = s;
  for (let i = 0; i < difference; i++) {
    result += v;
  }
  return result;
}
