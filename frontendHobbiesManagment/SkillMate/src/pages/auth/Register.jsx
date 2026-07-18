import React from "react";
import { useForm } from "react-hook-form";
import { useNavigate, Link } from "react-router-dom";
import { useRegisterMutation } from "../../api/Api";
import "./Register.css";

const Register = () => {
  const navigate = useNavigate();
  const [registerUser, { isLoading, error: registerError }] = useRegisterMutation();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm();

  const onSubmit = async (data) => {
    try {
      const formattedData = {
        ...data,
        idNumber: Number(data.idNumber),
        description: data.description || "",
      };
      await registerUser(formattedData).unwrap();
      alert("Registration completed successfully! You can now log in.");
      navigate("/");
    } catch (err) {
      console.error("Register failed:", err);
    }
  };
  return (
    <div className="register-container">
      <div className="register-paper">
        <h2 className="register-title">System Registration</h2>

        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="register-form-box">

            {/* Full Name */}
            <div className="form-group">
              <label>Full Name:</label>
              <input
                type="text"
                className="form-input"
                {...register("name", {
                  required: "Name is a required field",
                  pattern: {
                    value: /^[a-zA-Zא-ת\s]+$/,
                    message: "Name must contain letters only"
                  }
                })}
              />
              {errors.name && <p className="error-text">{errors.name.message}</p>}
            </div>

            {/* ID Number */}
            <div className="form-group">
              <label>ID Number:</label>
              <input
                type="text"
                className="form-input"
                {...register("idNumber", {
                  required: "ID Number is a required field",
                  pattern: {
                    value: /^\d{9}$/,
                    message: "ID Number must be exactly 9 digits"
                  }
                })}
              />
              {errors.idNumber && <p className="error-text">{errors.idNumber.message}</p>}
            </div>

            {/* Email */}
            <div className="form-group">
              <label>Email Address:</label>
              <input
                type="email"
                className="form-input"
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
              <input type="password" className="form-input" {...register("password", { required: "Password is a required field", minLength: { value: 6, message: "Minimum 6 characters required" } })} />
              {errors.password && <p className="error-text">{errors.password.message}</p>}
            </div>

            {/* Row: City & Phone */}
            <div className="form-row">
              <div className="form-group">
                <label>City:</label>
                <input type="text" className="form-input" {...register("city", { required: "City is a required field" })} />
                {errors.city && <p className="error-text">{errors.city.message}</p>}
              </div>
              <div className="form-group">
                <label>Phone Number:</label>
                <input
                  type="text"
                  className="form-input"
                  {...register("phone", {
                    required: "Phone number is a required field",
                    minLength: {
                      value: 9,
                      message: "Phone number must be at least 9 digits"
                    },
                    pattern: {
                      value: /^[0-9+\-\s]+$/,
                      message: "Phone number must contain digits only"
                    }
                  })}
                />
                {errors.phone && <p className="error-text">{errors.phone.message}</p>}
              </div>
            </div>

            {/* Row: Account Type & Gender */}
            <div className="form-row">
              <div className="form-group">
                <label>Account Type:</label>
                <select className="form-select" {...register("selectedRole", { required: "Please select a role" })}>
                  <option value="">Select...</option>
                  <option value="LEARNER">Learner</option>
                  <option value="MENTOR">Mentor</option>
                  <option value="MENTOR_AND_LEARNER">Both (Mentor & Learner)</option>
                </select>
                {errors.selectedRole && <p className="error-text">{errors.selectedRole.message}</p>}
              </div>
              <div className="form-group">
                <label>Gender:</label>
                <select className="form-select" {...register("gender", { required: "Please select a gender" })}>
                  <option value="">Select...</option>
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                </select>
                {errors.gender && <p className="error-text">{errors.gender.message}</p>}
              </div>
            </div>

            {/* Birthday */}
            <div className="form-group">
              <label>Date of Birth:</label>
              <input
                type="date"
                className="form-input"
                {...register("birthday", {
                  required: "Birthdate is a required field",
                  validate: (value) => {
                    const selectedDate = new Date(value);
                    const today = new Date();
                    today.setHours(0, 0, 0, 0);
                    return selectedDate < today || "Birthdate must be in the past";
                  }
                })}
              />
              {errors.birthday && <p className="error-text">{errors.birthday.message}</p>}
            </div>

            {/* Description */}
            <div className="form-group">
              <label>Tell us a bit about yourself:</label>
              <textarea className="form-textarea" {...register("description")} />
            </div>

            {registerError && (
              <div className="alert-error">
                Registration failed. Email or ID number might already exist in the system.
              </div>
            )}

            <button type="submit" className="register-submit-btn" disabled={isLoading}>
              {isLoading ? "Registering..." : "Register Now"}
            </button>

            {/* Switch to Login */}
            <div className="auth-switch-container">
              <p className="auth-switch-text">
                Already have an account?{" "}
                <Link to="/auth/login" className="auth-switch-link">
                  Click here to log in
                </Link>
              </p>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
};

export default Register;