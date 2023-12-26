import React from 'react';
import { connect } from "react-redux";
import './App.css';
import {getRides} from './redux/ride-selectors';
import {getFairyTales} from './redux/fairy-tale-selectors';
import {getStands} from './redux/stand-selectors';
import {getVisitors} from './redux/visitor-selectors';

import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col"
import Card from "react-bootstrap/Card";
import Table from "react-bootstrap/Table";
import StatisticsCard from "./StatisticsCard"

class Statistics extends React.Component {


  constructor(props) {
    super(props);
  }
  
    render() {
        const rideStatistics = this.props.rides.map(ride => 
            <Col xs={6} md={4} lg={3} key={ride.id}>
                <StatisticsCard ride={ride} />
            </Col>
            )

      return <Container fluid>
          <Row>
          <Col xs={6} md={4} lg={3}>
            <Card style={{ "margin": "10px 0"}}>
                <Card.Body>
                    <Card.Title>Park</Card.Title>
                    <Table striped bordered hover size="sm">
                        <tbody>
                            <tr>
                                <td>Bezoekers in park</td>
                                <td>{this.props.visitors.length}</td>
                            </tr>
                        </tbody>
                    </Table>
                </Card.Body>
            </Card>
            </Col>
              {rideStatistics}
          </Row>
      </Container>;
    }
  }

  const mapStateToProps = state => {
    const rides = getRides(state);
    const fairyTales = getFairyTales(state);
    const stands = getStands(state);
    const visitors = getVisitors(state);
    return { rides: rides, fairyTales: fairyTales, stands: stands, visitors: visitors };
  };

  export default connect(mapStateToProps)(Statistics);