import './App.css';
import LiveMap from "./LiveMap"
import Music from "./Music"
import Statistics from "./Statistics";
import React from 'react';
import axios from 'axios';
import { connect } from "react-redux";
import { setFairyTales } from "./redux/fairy-tale-actions";
import { setRides } from "./redux/ride-actions";
import { setStands } from "./redux/stand-actions";
import { setVisitors } from "./redux/visitor-actions";


class App extends React.Component{

  constructor(props) {
    super(props);
    this.getFairyTales();
    setInterval(() => this.getRides(), 1000);
    this.getStands();
    setInterval(() => this.getVisitors(), 1000);
  }

  getFairyTales = async () => {
    let response = await axios.get('http://localhost:49980/api/v1/fairy-tales');
    this.props.setFairyTales(response.data);
  }

  getRides = async () => {
    let response = await axios.get('http://localhost:49981/api/v1/rides');
    this.props.setRides(response.data);
  }

  getStands = async () => {
    let response = await axios.get('http://localhost:49982/api/v1/stands');
    this.props.setStands(response.data);
  }

  getVisitors = async () => {
    let response = await axios.get('http://localhost:49984/api/v1/visitors');
    this.props.setVisitors(response.data);
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

export default connect(null, {setRides, setFairyTales, setStands, setVisitors},)(App);
