function drawCharacter() {

    var writer = HanziWriter.create('draw_board', '轮', {
      charDataLoader: function() {
        data = JSON.parse(Android.getJSON())
        console.log(data)
        return data
      },
      width: 200,
      height: 200,
      padding: 5,
      delayBetweenLoops: 3000
    });

    writer.loopCharacterAnimation();

}

