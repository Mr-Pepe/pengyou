function init() {
    wasm_bindgen('hanzi_lookup_bg.wasm').then(value => {wasm_bindgen.setWasm(value); Android.isLoaded();});
}

function feedback() {
    Android.feedback(wasm_bindgen.lookup([[[83.5, 106], [89.5, 106], [107.5, 106], [107.5, 106]]], 8));
}

