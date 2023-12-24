
export const getVisitorsState = store => store.visitors;

export const getVisitorsList = store =>
getVisitorsState(store) ? getVisitorsState(store).allIds : [];

export const getVisitorById = (store, id) =>
getVisitorsState(store) ? { ...getVisitorsState(store).byIds[id], id } : {};

export const getVisitors = store =>
getVisitorsList(store).map(id => getVisitorById(store, id));