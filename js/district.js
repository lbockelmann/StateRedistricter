/*global randomColor*/

class District {
  constructor(id, name, stateAbbr, featureGeoJson) {
    this.id = id.startsWith('d') ? id.substring(1) : id;
    this.name = name;
    this.stateAbbr = stateAbbr;
    this.color = this.randomColor(id);
    console.log(this.color);
    this.geojson = featureGeoJson;
    this.type = 'feature';
    this.properties = this.geojson.properties;
    this.geometry = this.geojson.geometry;
    this.map = undefined;
    this.layer = undefined;
    this.precincts = {};
    this.rendered = false;
  }
  static validateDid(did) {
    return did.startsWith('d') ? did.substring(1) : did;
  }

  render(map) {
    console.log('Rendering district ' + this.id);
    this.map = map;
    this.layer = map.renderGeoJson(this.geojson, {
      style: this.getDistrictStyles.bind(this),
    });
    this.layer.on('mouseover', this.addDataToSidebar.bind(this));
    this.layer.on('mouseout', this.removeDataFromSidebar.bind(this));

    this.rendered = true;
  }

  addDataToSidebar() {
    let year = $('#year-select').attr('code');
    year = parseInt(year);
    $('#dem-vote-count-before').html(map.currentState.electionData[year][this.id].totalDemVotes.toLocaleString());
    $('#rep-vote-count-before').html(map.currentState.electionData[year][this.id].totalRepVotes.toLocaleString());
  }

  removeDataFromSidebar() {
    $('#dem-vote-count-before').html('??');
    $('#rep-vote-count-before').html('??');
  }

  getDistrictStyles() {
    return {
      fillColor: this.color, weight: 1, color: '#ddd', dashArray: '4', fillOpacity: 0,
    };
  }

  unrender() {
    if (this.rendered) {
      this.map.removeLayer(this.layer);
      this.rendered = false;
      this.layer = undefined;
    }
  }

  calculatePopulation() {
    let sum = 0;
    for (let p of Object.values(this.precincts)) {
      sum += Number(p.population);
    }
    return sum;
  }

  randomColor(id) {
    if (id.startsWith('d')) {
      id = id.substring(1);
    }
    let colorList = ["#000000", "#FFFF00", "#1CE6FF", "#FF34FF", "#FF4A46",
      "#008941", "#006FA6", "#A30059", "#FFDBE5", "#7A4900", "#0000A6",
      "#63FFAC", "#B79762", "#004D43", "#8FB0FF", "#997D87", "#5A0007",
      "#809693", "#FEFFE6", "#1B4400", "#4FC601", "#3B5DFF", "#4A3B53",
      "#FF2F80", "#61615A", "#BA0900", "#6B7900", "#00C2A0", "#FFAA92",
      "#FF90C9", "#B903AA", "#D16100", "#DDEFFF", "#000035", "#7B4F4B",
      "#A1C299", "#300018", "#0AA6D8", "#013349", "#00846F", "#372101",
      "#FFB500", "#C2FFED", "#A079BF", "#CC0744", "#C0B9B2", "#C2FF99",
      "#001E09", "#00489C", "#6F0062", "#0CBD66", "#EEC3FF", "#456D75",
      "#B77B68", "#7A87A1", "#788D66", "#885578", "#FAD09F", "#FF8A9A",
      "#D157A0", "#BEC459", "#456648", "#0086ED", "#886F4C", "#34362D",
      "#B4A8BD", "#00A6AA", "#452C2C", "#636375", "#A3C8C9", "#FF913F",
      "#938A81", "#575329", "#00FECF", "#B05B6F", "#8CD0FF", "#3B9700",
      "#04F757", "#C8A1A1", "#1E6E00", "#7900D7", "#A77500", "#6367A9",
      "#A05837", "#6B002C", "#772600", "#D790FF", "#9B9700", "#549E79",
      "#FFF69F", "#201625", "#72418F", "#BC23FF", "#99ADC0", "#3A2465",
      "#922329", "#5B4534", "#FDE8DC", "#404E55", "#0089A3", "#CB7E98",
      "#A4E804", "#324E72", "#6A3A4C",];
    return colorList[id];

  }
}
