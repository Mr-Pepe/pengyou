var writer;


function initCharacter() {

    writer = HanziWriter.create('draw_board', '', {
      charDataLoader: function() {
        data = JSON.parse(Android.getJSON())
        return data
      },
      width: 980,
      height: 980,
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
    writer.quiz({
      onMistake: function(strokeData) {
//        console.log('Oh no! you made a mistake on stroke ' + strokeData.strokeNum);
//        console.log("You've made " + strokeData.mistakesOnStroke + " mistakes on this stroke so far");
//        console.log("You've made " + strokeData.totalMistakes + " total mistakes on this quiz");
//        console.log("There are " + strokeData.strokesRemaining + " strokes remaining in this character");
      },
      onCorrectStroke: function(strokeData) {
//        console.log('Yes!!! You got stroke ' + strokeData.strokeNum + ' correct!');
//        console.log('You made ' + strokeData.mistakesOnStroke + ' mistakes on this stroke');
//        console.log("You've made " + strokeData.totalMistakes + ' total mistakes on this quiz');
//        console.log('There are ' + strokeData.strokesRemaining + ' strokes remaining in this character');
      },
      onComplete: function(summaryData) {
//        console.log('You did it! You finished drawing ' + summaryData.character);
//        console.log('You made ' + summaryData.totalMistakes + ' total mistakes on this quiz');
        Android.quizFinished();
      }
    });
}
