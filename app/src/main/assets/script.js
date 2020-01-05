function drawCharacter() {
    var writer = HanziWriter.create('character-target-div', '上', {
      charDataLoader: function() {
        data = JSON.parse(Android.getJSON())
        console.log(data)
        return data
      }
    });

    writer.loopCharacterAnimation();
}
