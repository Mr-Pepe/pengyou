var writer;


function initCharacter() {

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
    Android.resetFinished();
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

function showCharacter() {
    writer.showCharacter();
}

function hideCharacter() {
    writer.hideCharacter();
}
