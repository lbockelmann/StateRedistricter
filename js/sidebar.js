/*global startAlgorithmHandler pauseAlgorithm L:true*/

/* exports Sidebar */

class Sidebar {

  constructor(map, tagID) {
    this.map = map;
    this.map.addSidebar(this);
    this.tagID = tagID;
    this.sideButtonIDs = [];
    this.sliders = [];
    this.sidebar = L.control.sidebar(tagID).addTo(map.rawMap());
    this.randomizeButton = undefined;
    this.functionButton = undefined;
    this.showing = false;
  }

  on(event, handler) {
    this.sidebar.on(event, handler);
  }

  hide() {
    this.sidebar.close();
  }

  show(id) {
    this.sidebar.open(id);
  }

  addExistingButtonID(id) {
    this.sideButtonIDs.push(id);
  }

  addExistingSliderID(id) {
    this.sliders.push(id);
  }

  disableSliders(list) {
    if (typeof list === 'string') {
      $(list).attr('disabled', true);
      $(list).css('background-color', '#ddd');
    } else if (typeof list === 'undefined') {
      for (let slider of this.sliders) {
        $(slider).attr('disabled', true);
        $(slider).css('background-color', '#ddd');
      }
    } else {
      for (let slider of list) {
        if ($(slider).length === 0) {
          throw 'Slider not found: ' + slider;
        }
        $(slider).attr('disabled', true);
        $(slider).css('background-color', '#ddd');
      }
    }
    $('#algo-select').css('color', 'grey');
    $('#year-select').css('color', 'grey');
  }

  enableSliders(list) {
    if (typeof list === 'string') {
      $(list).attr('disabled', false);
      $(list).css('background-color', '#000');
    } else if (typeof list === 'undefined') {
      for (let slider of this.sliders) {
        $(slider).attr('disabled', false);
        $(slider).css('background-color', '#000');

      }
    } else {
      for (let slider of list) {
        if ($(slider).length === 0) {
          throw 'Slider not found: ' + slider;
        }
        $(slider).attr('disabled', false);
        $(slider).css('background-color', '#000');
      }
    }
    $('#algo-select').css('color', 'white');
    $('#year-select').css('color', 'white');
  }

  disableRandomize() {
    this.randomizeButton.attr('disabled', true);
  }

  enableRandomize() {
    this.randomizeButton.attr('disabled', false);
  }

  disableUserActions() {
    $('#mapLoadButton').attr('disabled', true);
    $('#mapDeleteButton').attr('disabled', true);
    $('#accordion2').hide();
    $('#accordion').hide();
  }

  enableUserActions() {
    $('#mapLoadButton').attr('disabled', false);
    $('#mapDeleteButton').attr('disabled', false);
    $('#accordion2').show();
    $('#accordion').show();
  }

  turnStartOff() {
    this.functionButton.removeClass('btn-primary');
    this.functionButton.addClass('btn-warning');
    this.functionButton.html('Pause Algorithm');
    this.functionButton.off();
    this.functionButton.click(pauseAlgorithm);
  }

  turnStartOn() {
    this.functionButton.removeClass('btn-warning');
    this.functionButton.addClass('btn-primary');
    this.functionButton.html('Start Algorithm');
    this.functionButton.off();
    this.functionButton.click(startAlgorithmHandler);
  }

  resetSidebar() {
    this.disableSideButtons();
    this.hide();
    $('#search-results').html('');
  }

  enableSideButtons() {
    for (let buttonID of this.sideButtonIDs) {
      if ($(buttonID).length === 0) {
        throw 'Button not found: ' + buttonID;
      }
      $(buttonID).removeClass('disabled');
    }
  }

  showRedistrictingStatistics() {
    console.log(moveData);
    $('#move-done-count').html(moveData.performed);
    $('#move-undone-count').html(moveData.undone);



    let newValue = Number(moveData.currentValue).toPrecision(3);
    let originalVal = Number(moveData.firstValue).toPrecision(3);

    let r = 255 * (1 - originalVal);
    let g = 255 * (originalVal);
    let b = 0;
    $('#fn-value-before').css('color', `rgb(${r}, ${g}, 0)`);
    $('#fn-value-before').html(originalVal);

    r = 255 * (1 - newValue);
    g = 255 * (newValue);
    b = 0;
    $('#fn-value-after').css('color', `rgb(${r}, ${g}, 0)`);
    $('#fn-value-after').html(newValue);
  }

  disableSideButtons() {
    for (let buttonID of this.sideButtonIDs) {
      if ($(buttonID).length === 0) {
        throw 'Button not found: ' + buttonID;
      }
      $(buttonID).addClass('disabled');
    }
  }
}
