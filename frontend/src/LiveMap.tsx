import React from 'react';
import { ConnectedProps, connect } from "react-redux";
import './App.css';
import "./LiveMap.css";

import Map from 'ol/Map';
import {Style, Icon, Circle, Fill, Stroke} from 'ol/style';
import Tile from 'ol/layer/Tile';
import OSM from 'ol/source/OSM';
import View from 'ol/View';
import {fromLonLat} from 'ol/proj';
import Feature from 'ol/Feature';
import Point from 'ol/geom/Point';
import VectorSource from 'ol/source/Vector';
import VectorImageLayer from 'ol/layer/VectorImage';
import { fairyTaleDto, rideDto, standDto, visitorDto } from './services/openapi';
import { RootState } from './redux/store';
import {calculateLat, calculateLon} from "./helpers";

var rideIconStyle = new Style({
  image: new Icon({
      anchor: [0.5, 44],
      anchorXUnits: 'fraction',
      anchorYUnits: 'pixels',
      src: 'data/ride-icon.png'
  })
});

var fairyTaleIconStyle = new Style({
  image: new Icon({
      anchor: [0.5, 44],
      anchorXUnits: 'fraction',
      anchorYUnits: 'pixels',
      src: 'data/tale-icon.png'
  })
});

var standIconStyle = new Style({
  image: new Icon({
      anchor: [0.5, 44],
      anchorXUnits: 'fraction',
      anchorYUnits: 'pixels',
      src: 'data/stand-icon.png'
  })
});

var fill = new Fill({
  color: 'rgba(240,240,240,0.4)'
});
var stroke = new Stroke({
  color: '#333333',
  width: 1.0
});

var visitorIconStyle = new Style({
  image: new Circle({
    fill: fill,
      stroke: stroke,
      radius: 3
  })
});

const mapStateToProps = (state: RootState) => {
  const rides = state.rideReducer.entities;
  const fairyTales = state.fairyTaleReducer.entities;
  const stands = state.standReducer.entities;
  const visitors = state.visitorReducer.entities;
  return { rides: rides, fairyTales: fairyTales, stands: stands, visitors: visitors };
};

const connector = connect(mapStateToProps)

type PropsFromRedux = ConnectedProps<typeof connector>

type Props = PropsFromRedux & {
  visitors: visitorDto[]
  rides: rideDto[]
  stands: standDto[]
  fairyTales: fairyTaleDto[]
}

class LiveMap extends React.Component<Props> {
  map!: React.RefObject<HTMLDivElement>
  eftelingMap!: Map

  visitorsLayer = this.getEmptyLayer();
  ridesLayer = this.getEmptyLayer();
  fairyTalesLayer = this.getEmptyLayer();
  standsLayer = this.getEmptyLayer();
  
  constructor(props: Props) {
    super(props);
    this.map = React.createRef<HTMLDivElement>()
  }

  componentDidMount() {
    console.log(this.map)
    this.eftelingMap = new Map({
      target: this.map.current as any,
      layers: [
          new Tile({
              source: new OSM()
          })
      ],
      view: new View({
          center: fromLonLat([5.0499, 51.6499]),
          zoom: 16
      })
    });
    this.visitorsLayer.setStyle(visitorIconStyle);
    this.ridesLayer.setStyle(rideIconStyle);
    this.fairyTalesLayer.setStyle(fairyTaleIconStyle);
    this.standsLayer.setStyle(standIconStyle);
    
    this.eftelingMap.addLayer(this.ridesLayer);
    this.eftelingMap.addLayer(this.fairyTalesLayer);
    this.eftelingMap.addLayer(this.standsLayer);
    this.eftelingMap.addLayer(this.visitorsLayer);
  }

  getEmptyLayer(){
      var vectorSource = new VectorSource({
        features: []
      });

      var vectorLayer = new VectorImageLayer({
        source: vectorSource
      });

      return vectorLayer;
    }

  getFeature(id: string, lon: number, lat: number){
      var iconFeature = new Feature({
        geometry: new Point(
            fromLonLat([lon, lat])
        )
      });
  
      iconFeature.setId(id);
      return iconFeature;
  }

  updateVisitors(){

    var vectorSource = new VectorSource({
      features: []
    });

    this.props.visitors.forEach(visitor => {
      var iconFeature = this.getFeature(visitor.id, calculateLon(visitor.location.x, visitor.location.y), calculateLat(visitor.location.x, visitor.location.y));
      vectorSource.addFeature(iconFeature);
    });

    this.visitorsLayer.setSource(vectorSource);
  }

  updateRides(){
    var ridesSource = this.ridesLayer.getSource();
    this.props.rides.forEach(ride => {

      var mapRide = ridesSource?.getFeatureById(ride.id);

      if(mapRide == null){
        var iconFeature = this.getFeature(ride.id, calculateLon(ride.location.x, ride.location.x), calculateLat(ride.location.x, ride.location.y));
        ridesSource?.addFeature(iconFeature);
      } else {
        console.log(`${ride.name}: ${calculateLat(ride.location.x, ride.location.y)}; ${calculateLon(ride.location.x, ride.location.y)}`);
        (mapRide.getGeometry() as any).setCoordinates(fromLonLat([calculateLon(ride.location.x, ride.location.y), calculateLat(ride.location.x, ride.location.y)]));
      }
    });
  }

  updateFairyTales(){
    var fairyTalesSource = this.fairyTalesLayer.getSource();
    this.props.fairyTales.forEach(tale => {

      var mapTale = fairyTalesSource?.getFeatureById(tale.id);

      if(mapTale == null){
        var iconFeature = this.getFeature(tale.id, calculateLon(tale.location.x, tale.location.y), calculateLat(tale.location.x, tale.location.y));
        fairyTalesSource?.addFeature(iconFeature);
      } else {
        (mapTale.getGeometry() as any).setCoordinates(fromLonLat([calculateLon(tale.location.x, tale.location.y), calculateLat(tale.location.x, tale.location.y)]));
      }
    });
  }

  updateStands(){
    var standsSource = this.standsLayer.getSource();
    this.props.stands.forEach(stand => {

      var mapStand = standsSource?.getFeatureById(stand.id);

      if(mapStand == null){
        var iconFeature = this.getFeature(stand.id, calculateLon(stand.location.x, stand.location.y), calculateLat(stand.location.x, stand.location.y));
        standsSource?.addFeature(iconFeature);

      } else {
        (mapStand.getGeometry() as any).setCoordinates(fromLonLat([calculateLon(stand.location.x, stand.location.y), calculateLat(stand.location.x, stand.location.y)]));
      }
    });
  }
  
    render() {
        if(this.eftelingMap != null){
          this.updateVisitors();
          this.updateFairyTales();
          this.updateRides();
          this.updateStands();
        }

      return <div ref={this.map} className="ol-map"></div>;
    }
  }

  export default connector(LiveMap);