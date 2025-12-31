import { configureStore } from '@reduxjs/toolkit'
import rideReducer from "./reducers/rides"
import fairyTaleReducer from './reducers/fairy-tales'
import standReducer from './reducers/stands'
import visitorReducer from './reducers/visitors'
import employeeReducer from './reducers/employees'
import { TypedUseSelectorHook, useDispatch, useSelector } from 'react-redux'

const store = configureStore({
    reducer: {
        rideReducer: rideReducer,
        fairyTaleReducer: fairyTaleReducer,
        standReducer: standReducer,
        visitorReducer: visitorReducer,
        employeeReducer: employeeReducer
    }
})

export default store

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch

export const useAppDispatch: () => AppDispatch = useDispatch
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector