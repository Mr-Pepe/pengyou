var writer;


function initCharacter() {

    var width = 300;
    var height = 300;

    writer = HanziWriter.create('draw_board', '', {
      charDataLoader: function() {
        data = JSON.parse(Android.getJSON())
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
