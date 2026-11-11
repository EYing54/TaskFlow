import request from "../utils/request";

export const userLogin = (username, password) => {
  return request.post("/login", {
    username: username,
    password: password,
  });
};
