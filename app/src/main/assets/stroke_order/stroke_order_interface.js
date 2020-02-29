var writer;


function initCharacter() {

    var width = Android.getSize();
    var height = Android.getSize();

    writer = HanziWriter.create('draw_board', '', {
      charDataLoader: function() {
        data = JSON.parse(Android.getJSON())
        return data
      },
      width: width,
      height: height,
      showOutline: true,
      showCharacter: false,
      strokeColor: Android.getStrokeColor(),
      outlineColor: Android.getOutlineColor(),
      drawingColor: Android.getStrokeColor(),
      drawingWidth: 10
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
    writer.hideCharacter();
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
    Android.showCharacterFinished();
}

function hideCharacter() {
    writer.hideCharacter();
}

function startQuiz() {
    writer.quiz();
}
