import './App.css';
import LiveMap from "./LiveMap"
import Music from "./Music"
import Statistics from "./Statistics";
import React, { useEffect } from 'react';
import { fetchRides } from './redux/reducers/rides';
import { visitorsUpdated } from './redux/reducers/visitors';
import { fetchStands } from './redux/reducers/stands';
import { fetchFairyTales } from './redux/reducers/fairy-tales';
import { fetchEmployees } from './redux/reducers/employees';
import { AppDispatch, useAppDispatch } from './redux/store';

function App (){

  const dispatch: AppDispatch = useAppDispatch()
  dispatch(fetchFairyTales())
  dispatch(fetchStands())

  useEffect(() => {
    const interval = setInterval(() => {
      dispatch(fetchRides())
    }, 1000);
    return () => clearInterval(interval);
  }, [dispatch]);

  useEffect(() => {
    const eventSource = new EventSource('http://localhost:49984/api/v1/visitors/stream');
    eventSource.onmessage = (event) => {
      dispatch(visitorsUpdated(JSON.parse(event.data)));
    };
    return () => eventSource.close();
  }, [dispatch]);

  useEffect(() => {
    const interval = setInterval(() => {
      dispatch(fetchEmployees())
    }, 500);
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
