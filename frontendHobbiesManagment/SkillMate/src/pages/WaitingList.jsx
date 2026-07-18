import React from "react";
import {
  useDeleteWaitinglistEntryMutation,
  useEnrollFromWaitlistMutation,
  useGetLearnerWaitlistQuery,
} from "../api/Api";
import "./WaitingList.css";

export const WaitingList = () => {
  const learnerId = sessionStorage.getItem("userId");
  const { data: waitlist = [], isLoading } = useGetLearnerWaitlistQuery(learnerId, {
    skip: !learnerId,
  });
  const [deleteEntry] = useDeleteWaitinglistEntryMutation();
  const [enrollFromWaitlist] = useEnrollFromWaitlistMutation();

  const handleRemove = async (entryId) => {
    if (!window.confirm("Are you sure you want to remove this from the waiting list?")) {
      return;
    }

    try {
      await deleteEntry(entryId).unwrap();
    } catch (err) {
      console.error("Failed to remove waiting-list entry:", err);
    }
  };

  const handleEnroll = async (entryId) => {
    try {
      await enrollFromWaitlist(entryId).unwrap();
      alert("You have been successfully enrolled! A session has been scheduled for you.");
    } catch (err) {
      console.error("Failed to enroll from waiting list:", err);
      const message = err?.data?.message || err?.message || "An error occurred. Please try again.";
      alert("Failed to enroll: " + message);
    }
  };

  if (!learnerId) {
    return (
      <div className="waiting-list-container">
        <p className="empty-message">Please log in to view your waiting list.</p>
      </div>
    );
  }

  if (isLoading) return <p>Loading waiting list...</p>;

  return (
    <div className="waiting-list-container">
      <h2>My Waiting List</h2>
      {waitlist.length === 0 ? (
        <p className="empty-message">No items found in the waiting list at the moment.</p>
      ) : (
        <table className="waitlist-table">
          <thead>
            <tr>
              <th>Skill</th>
              <th>Mentor</th>
              <th>Status</th>
              <th>Queue Position</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {waitlist.map((item) => (
              <tr key={item.id}>
                <td>{item.skillName}</td>
                <td>{item.mentorName}</td>
                <td>
                  <span className={`status-${item.waitlistStatus?.toLowerCase()}`}>
                    {item.waitlistStatus}
                  </span>
                </td>
                <td>{item.queuePosition}</td>
                <td>
                  {item.waitlistStatus === "NOTIFIED" ? (
                    <button onClick={() => handleEnroll(item.id)} className="delete-btn">
                      Book now
                    </button>
                  ) : (
                    <button
                      onClick={() => handleRemove(item.id)}
                      className="delete-btn"
                    >
                      Remove
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default WaitingList;
