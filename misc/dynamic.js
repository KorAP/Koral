function filterCodes (e) {

  // Get parent element
  var parent = this.parentNode;

  // Get dd level 
  while (parent.nodeName !== 'DD')
    parent = parent.parentNode;

  // Get term
  var dt = parent.previousElementSibling;

  while (parent.nodeName !== 'TBODY')
    parent = parent.parentNode;

  // Show all parents first!
  var trs = parent.getElementsByTagName('tr');
  for (var tr = 0; tr < trs.length; tr++) {
    trs[tr].style.opacity = 1;
  };

  // The chosen object is already filtered - unfilter
  if (this.classList.contains('filtered')) {
    // remove filtered option
    
    dt.classList.remove('chosen');
    this.classList.remove('filtered');
    return;
  }

  // the chosen object is not filtered yet, but probably another
  else {
    var dl = dt.parentNode;

    // valid parameters
    var vps = dl.getElementsByClassName('valid-parameters');
    for (var i = 0; i < vps.length; i++) {
      vps[i].classList.remove('filtered');
    };

    var dts = dl.getElementsByTagName('dt');
    for (var i = 0; i < dts.length; i++) {
      dts[i].classList.remove('chosen');
    };
  };


  // Add filter parameter
  this.classList.add('filtered');
  dt.classList.add('chosen');

  var codes = this.getElementsByTagName('code');
  var codeVal = {
    '@type' : 1,
    'operands' : 1,
    'operation' : 1
  };

  // Add to associated array
  for (var c = 0; c < codes.length; c++) {
    codeVal[codes[c].firstChild.nodeValue] = 1;
  };

  var trs = parent.getElementsByTagName('tr');
  for (var tr = 0; tr < trs.length; tr++) {
    var nn = trs[tr].getElementsByTagName('td')[0].firstChild.nodeValue;

    if (codeVal[nn] !== undefined)
      continue;

    trs[tr].style.opacity = 0.3;
  };
};

window.onload = function (e) {
  var params = document.getElementsByClassName('valid-parameters');
  for (var l = 0; l < params.length; l++) {
    params[l].addEventListener(
      "click", filterCodes, false
    );
  };
};
