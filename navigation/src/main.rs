use pathfinding::prelude::astar;
use actix_web::{App, HttpServer, Result, web, HttpResponse, http::Error};
use serde::Deserialize;
use visitor_route::{Board, Pos};

#[derive(Deserialize)]
struct PFRequest {
    start_x: i16,
    start_y: i16,
    dest_x: i16,
    dest_y: i16
}

fn get_board() -> Board {
  return Board::new(vec![
    "21397X2",
    "1X19452",
    "62251X1",
    "1612179",
    "1348512",
    "61453X1",
    "7861243"], false);
}

fn get_path(start: Pos, dest: Pos) -> String {
  let board: Board = get_board();

  let result = astar(&start,
    |p| board.get_successors(p).iter().map(|s| (s.pos, s.cost)).collect::<Vec<_>>(),
    |p| ((p.0 - dest.0).abs() + (p.1 - dest.1).abs()) as u32,
    |p| *p==dest);

  let mut data = json::JsonValue::new_array();

  let points = result.expect("Something went wrong");

  for i in points.0.iter() {
    let mut jsonobj = json::JsonValue::new_object();
    jsonobj["x"] = i.0.into();
    jsonobj["y"] = i.1.into();
    data.push(jsonobj).expect("Error when writing values");
  };

  return data.dump();
}

async fn index(info: web::Json<PFRequest>) -> Result<HttpResponse, Error>  {
  let start = Pos(info.start_x, info.start_y);
  let dest = Pos(info.dest_x, info.dest_y);

  let path = self::get_path(start, dest);

  Ok(HttpResponse::Ok().body(path))
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    HttpServer::new(|| {
        App::new()
          .route("/", web::post().to(index))
    })
    .bind(("127.0.0.1", 49985))?
    .run()
    .await
}