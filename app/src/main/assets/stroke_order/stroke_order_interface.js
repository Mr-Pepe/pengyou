var writer;


function showCharacter() {

    var width = 300;
    var height = 300;

    writer = HanziWriter.create('draw_board', '', {
      charDataLoader: function() {
        data = JSON.parse(Android.getJSON())
        console.log(data)
        return data
      },
      width: width,
      height: height,
      showOutline: true,
      showCharacter: false
    });
}

function animateStroke() {
    writer.animateStroke(Android.getCurrentStroke(), {
    onComplete: function(data) {
        Android.strokeComplete();
    }
    });

}

function reset() {
    writer.resetRenderer();
}

function hideCharacter() {
    writer.hideCharacter();
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


