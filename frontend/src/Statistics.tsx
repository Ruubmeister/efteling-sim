import React from 'react';
import { ConnectedProps, connect } from "react-redux";
import './App.css';
import {getRides} from './redux/reducers/rides';

import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col"
import Card from "react-bootstrap/Card";
import Table from "react-bootstrap/Table";
import StatisticsCard from "./StatisticsCard"
import { RootState } from './redux/store';
import { rideDto } from './services/openapi';
import { getFairyTales } from './redux/reducers/fairy-tales';
import { getStands } from './redux/reducers/stands';
import { getVisitors } from './redux/reducers/visitors';

const mapStateToProps = (state: RootState) => {
    const rides = getRides(state);
    const fairyTales = getFairyTales(state);
    const stands = getStands(state);
    const visitors = getVisitors(state);
    return { rides: rides, fairyTales: fairyTales, stands: stands, visitors: visitors };
  };

const connector = connect(mapStateToProps)

type PropsFromRedux = ConnectedProps<typeof connector>

type Props = PropsFromRedux & {}

class Statistics extends React.Component<Props> {

    render() {
        const rideStatistics = this.props.rides.map((ride: rideDto) => 
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

  export default connect(mapStateToProps)(Statistics);