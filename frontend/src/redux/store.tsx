import { configureStore } from '@reduxjs/toolkit'
import rideReducer from "./reducers/rides"
import fairyTaleReducer from './reducers/fairy-tales'
import standReducer from './reducers/stands'
import visitorReducer from './reducers/visitors'

const store = configureStore({
    reducer: {
        rideReducer: rideReducer,
        fairyTaleReducer: fairyTaleReducer,
        standReducer: standReducer,
        visitorReducer: visitorReducer
    }
})

export default store

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch
