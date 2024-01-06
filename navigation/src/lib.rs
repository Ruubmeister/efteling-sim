#[derive(Clone, Copy, Debug, Eq, Hash, Ord, PartialEq, PartialOrd)]
pub struct Pos(pub i16, pub i16);

pub struct Board {
    pub width: u8,
    pub height: u8,
    pub data: Vec<Vec<Option<u8>>>,
    pub allow_diagonal: bool
}

impl Board {
    pub fn new(board_lines: Vec<&str>, allow_diagonal: bool) -> Board {
        let width = board_lines[0].len() as u8;
        let height = board_lines.len() as u8;
        let mut data = Vec::new();
        for board_line in board_lines {
            let mut row: Vec<Option<u8>> = Vec::new();
            for c in board_line.chars() {
                match c {
                    'X' => row.push(None),
                    '1'..='9' => row.push(Some(c as u8 - b'0')),
                    _ => panic!("invalid character")
                }
            }
            data.push(row);
        }
        Board {width, height, data, allow_diagonal}
    }

    pub fn get_successors(&self, position: &Pos) -> Vec<Successor> {
        let mut successors = Vec::new();
        for dx in -1i16..=1 {
            for dy in -1i16..=1 {
                if self.allow_diagonal {
                    if dx == 0 && dy == 0 {
                        continue;
                    }
                }
                else {
                    // Omit diagonal moves (and moving to the same position)
                    if (dx + dy).abs() != 1 {
                        continue;
                    }
                }
                let new_position = Pos(position.0 + dx, position.1 + dy);
                if new_position.0 < 0 || new_position.0 >= self.width.into() || new_position.1 < 0 || new_position.1 >= self.height.into() {
                    continue;
                }
                let board_value = self.data[new_position.1 as usize][new_position.0 as usize];
                if let Some(board_value) = board_value {
                    successors.push(Successor { pos: new_position, cost: board_value as u32});
                }
            }
        }

        successors
    }
}

#[derive(Clone, Copy, Debug, Eq, PartialEq, PartialOrd)]
pub struct Successor {
    pub pos: Pos,
    pub cost: u32,
}
// Used to make writing tests easier
impl PartialEq<(Pos, u32)> for Successor {
    fn eq(&self, other: &(Pos, u32)) -> bool {
        self.pos == other.0 && self.cost == other.1
    }
}