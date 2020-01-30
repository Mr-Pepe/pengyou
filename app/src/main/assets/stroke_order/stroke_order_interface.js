var writer;


function showCharacter() {

    var width = 300;
    var height = 300
    var padding = 5;

    writer = HanziWriter.create('draw_board', '', {
      charDataLoader: function() {
        data = JSON.parse(Android.getJSON())
        console.log(data)
        return data
      },
      width: width,
      height: height,
      padding: padding,
      showOutline: true,
      showCharacter: false
    });
}

function animate(){
    writer.animateCharacter();
}

function showOutline() {
    writer.showOutline();
}

function hideOutline() {
    writer.hideOutline();
}


