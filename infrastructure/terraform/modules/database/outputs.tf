output "users_table_name" {
  value = aws_dynamodb_table.users.name
}

output "rides_table_name" {
  value = aws_dynamodb_table.rides.name
}

output "bookings_table_name" {
  value = aws_dynamodb_table.bookings.name
}

output "table_names" {
  value = {
    users    = aws_dynamodb_table.users.name
    rides    = aws_dynamodb_table.rides.name
    bookings = aws_dynamodb_table.bookings.name
  }
}
