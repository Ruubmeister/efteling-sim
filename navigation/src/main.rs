mod grid;

use pathfinding::prelude::astar;
use actix_web::{App, HttpServer, Result, web, HttpResponse, http::Error};
use serde::Deserialize;
use navigation::{Board, Pos};
use grid::get_grid;

#[derive(Deserialize)]
struct PFRequest {
    start_x: i32,
    start_y: i32,
    dest_x: i32,
    dest_y: i32
}

fn get_board() -> Board {
  let grid: Vec<&str> = get_grid();
  return Board::new(grid, false);
}

fn get_path(start: Pos, dest: Pos) -> String {
  let board: Board = get_board();

  let result = astar(&start,
    |p| board.get_successors(p).iter().map(|s| (s.pos, s.cost)).collect::<Vec<_>>(),
    |p| ((p.0 - dest.0).abs() + (p.1 - dest.1).abs()) as u32,
    |p| *p==dest);

  let mut data = json::JsonValue::new_array();

  let points = result.expect(&format!("No results for going from point {} {} to point {} {}", start.0, start.1, dest.0, dest.1));

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
          .route("/api/v1/navigate", web::post().to(index))
    })
    .bind(("0.0.0.0", 49985))?
    .run()
    .await
}