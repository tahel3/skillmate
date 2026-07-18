import React from "react";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";
import { useDispatch } from "react-redux";
import { useLoginMutation } from "../../api/Api";
import { setCredentials } from "../../features/AuthSlice";
import { Link } from "react-router-dom";
import "./Login.css";
import { notifyAuthChanged } from "../../hooks/useHooks";
const Login = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const [loginUser, { isLoading, error: loginError }] = useLoginMutation();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm();

  const onSubmit = async (data) => {
    try {
      const response = await loginUser(data).unwrap(); 
      dispatch(setCredentials({
        token: response.token,
        role: response.role,
        email: response.email,
         userId: response.userId 
      }));

     sessionStorage.setItem('token', response.token);
sessionStorage.setItem('user', JSON.stringify({
  userId: response.userId,
  role: response.role,
  email: response.email,
  name: response.name || null,
}));
window.dispatchEvent(new Event("storage"));
notifyAuthChanged();

      if (!response.hasProfile) {
        navigate("/profile");
      } else {
        navigate("/");
      }
    } catch (err) {
      console.error("Login failed:", err);
    }
  };

  return (
    <div className="login-container">
      <div className="login-paper">
        <h2 className="login-title">Login to System</h2>

        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="login-form-box">
            
            {/* Email Address */}
            <div className="form-group">
              <label>Email Address:</label>
              <input
                type="email"
                className="form-input"
                autoComplete="email"
                {...register("email", {
                  required: "Email is a required field",
                  pattern: {
                    value: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
                    message: "Please enter a valid email address"
                  }
                })}
              />
              {errors.email && <p className="error-text">{errors.email.message}</p>}
            </div>

            {/* Password */}
            <div className="form-group">
              <label>Password:</label>
              <input
                type="password"
                className="form-input"
                autoComplete="current-password"
                {...register("password", { required: "Password is a required field" })}
              />
              {errors.password && <p className="error-text">{errors.password.message}</p>}
            </div>

            {loginError && (
              <div className="alert-error">
                Login failed. Please verify that your email and password are correct.
              </div>
            )}

            <button type="submit" className="login-submit-btn" disabled={isLoading}>
              {isLoading ? "Logging in..." : "Login"}
            </button>
          </div>

          {/* Switch to Register */}
          <div className="auth-switch-container">
            <p className="auth-switch-text">
              Don't have an account yet?{" "}
              <Link to="/auth/register" className="auth-switch-link">
                Create a new account
              </Link>
            </p>
          </div>
        </form>
      </div>
    </div>
  );
};

export default Login;