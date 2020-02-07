function init() {
    wasm_bindgen('hanzi_lookup_bg.wasm').then(value => {wasm_bindgen.setWasm(value); Android.isLoaded();});
}

function search() {

    var iStroke;
    var iPoint;

    var strokes = [];
    try {
        for (iStroke = 0; iStroke < Android.getNumberOfStrokes(); iStroke++) {
            strokes.push([]);

            for (iPoint = 0; iPoint < Android.getNumberOfPoints(iStroke); iPoint++) {
                strokes[iStroke].push([]);
                strokes[iStroke][iPoint] = [Android.getX(iStroke, iPoint), Android.getY(iStroke, iPoint)];
            }
        }

        Android.updateProposedCharacters(wasm_bindgen.lookup(strokes, 8));
    }
    catch (e) {
        Android.updateProposedCharacters("");
        init();
    }
}
