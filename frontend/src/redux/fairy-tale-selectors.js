
export const getFairyTalesState = store => store.fairyTales;

export const geFairyTalesList = store =>
getFairyTalesState(store) ? getFairyTalesState(store).allIds : [];

export const getFairyTaleById = (store, id) =>
getFairyTalesState(store) ? { ...getFairyTalesState(store).byIds[id], id } : {};

export const getFairyTales = store =>
geFairyTalesList(store).map(id => getFairyTaleById(store, id));