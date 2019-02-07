/*exports clickHandlerForSlider*/

function clickHandlerForSlider(sliderNumber) {
  return function () {
    console.log('outer');
    $('#slider' + sliderNumber).on('input', function ( /*event*/ ) {
      console.log('changed');
      let slider = $('#slider' + sliderNumber);
      let val = slider.val();
      let sliderValue = $('#slider' + sliderNumber + '-value');
      sliderValue.html(val);
      slider.html(val);
      let r = 255 * (1-val);
      let g = 205 * (val);
      let b = 0;
      console.log(`rgb(${r}, ${g}, 0)`);
      sliderValue.css('color', `rgb(${r}, ${g}, 0)`)
    });
  };
}

$(document).ready(function () {
  let sliders = [];
  let i = 1;
  let slider;
  while ((slider = $('#slider' + i)).length != 0) {
    sliders.push(slider);
    i++;
  }

  $('#randomize-button').click(function () {
    for (let slider of sliders) {
      slider.val(Math.round(Math.random() * 100) / 100);
      slider.trigger('input');
    }
  });
});
