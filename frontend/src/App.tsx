import './App.css';
import LiveMap from "./LiveMap"
import Music from "./Music"
import Statistics from "./Statistics";
import React from 'react';
import { ConnectedProps, connect } from "react-redux";
import { fetchRides } from './redux/reducers/rides';
import { fetchVisitors } from './redux/reducers/visitors';
import { fetchStands } from './redux/reducers/stands';
import { fetchFairyTales } from './redux/reducers/fairy-tales';

const mapDispatch = {fetchVisitors, fetchStands, fetchFairyTales, fetchRides}

const connector = connect(null, mapDispatch)

type PropsFromRedux = ConnectedProps<typeof connector>

type Props = PropsFromRedux & {}

class App extends React.Component<Props>{

  constructor(props: Props) {
    super(props);
    setInterval(() => props.fetchRides(), 30000);
    props.fetchFairyTales();
    props.fetchStands();
    setInterval(() => props.fetchVisitors(), 1000);
  }

  render() { 
    return <div className="App">
        <LiveMap/>
        <div className="footer">
          Ruben Lelieveld © - Efteling simulator - <Music url="/private/achtergrondmuziek.mp3" />
          </div> 
        <Statistics />
        </div>;
  };
}

export default connector(App);
