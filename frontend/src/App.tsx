import './App.css';
import LiveMap from "./LiveMap"
import Music from "./Music"
import Statistics from "./Statistics";
import React, { useEffect } from 'react';
import { fetchRides } from './redux/reducers/rides';
import { fetchVisitors } from './redux/reducers/visitors';
import { fetchStands } from './redux/reducers/stands';
import { fetchFairyTales } from './redux/reducers/fairy-tales';
import { AppDispatch, useAppDispatch } from './redux/store';

function App (){

  const dispatch: AppDispatch = useAppDispatch()
  dispatch(fetchFairyTales())
  dispatch(fetchStands())

  useEffect(() => {
    const interval = setInterval(() => {
      dispatch(fetchRides())
      dispatch(fetchVisitors())
    }, 1000);
    return () => clearInterval(interval);
  }, [dispatch]);

  return (
    <div className="App">
        <LiveMap/>
        <div className="footer">
          Ruben Lelieveld © - Efteling simulator - <Music url="/private/achtergrondmuziek.mp3" />
          </div> 
        <Statistics />
        </div>
  )
}

export default App;
