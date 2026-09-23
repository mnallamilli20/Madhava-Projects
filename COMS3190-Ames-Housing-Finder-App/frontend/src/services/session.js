function normalizeUser(user) {
  if (!user) {
    return null;
  }

  const id = user._id || user.id || user.userId;

  if (!id) {
    return null;
  }

  return {
    ...user,
    _id: String(id)
  };
}

export function getStoredUser() {
  const stored = localStorage.getItem("amesHousingUser");

  if (!stored) {
    return null;
  }

  try {
    const user = normalizeUser(JSON.parse(stored));

    if (!user) {
      localStorage.removeItem("amesHousingUser");
      return null;
    }

    return user;
  } catch (error) {
    localStorage.removeItem("amesHousingUser");
    return null;
  }
}

export function saveStoredUser(user) {
  const normalizedUser = normalizeUser(user);

  if (!normalizedUser) {
    localStorage.removeItem("amesHousingUser");
    return;
  }

  localStorage.setItem("amesHousingUser", JSON.stringify(normalizedUser));
}

export function clearStoredUser() {
  localStorage.removeItem("amesHousingUser");
}

export function userId(user = getStoredUser()) {
  return normalizeUser(user)?._id || "";
}

export function isSignedIn(user = getStoredUser()) {
  return Boolean(userId(user));
}

export function isStudent(user = getStoredUser()) {
  return user?.role === "Student" || user?.role === "Renter" || user?.role === "User";
}

export function isAdmin(user = getStoredUser()) {
  return user?.role === "Admin";
}

export function canManageListings(user = getStoredUser()) {
  return isAdmin(user);
}

export function canManageReviews(user = getStoredUser()) {
  return isAdmin(user);
}

export function canManageResources(user = getStoredUser()) {
  return isAdmin(user);
}

export function canUsePersonalFeatures(user = getStoredUser()) {
  return isSignedIn(user);
}

export function authPayload(user = getStoredUser()) {
  const id = userId(user);

  return {
    actorId: id
  };
}

export function userPayload(user = getStoredUser()) {
  const id = userId(user);

  return {
    userId: id,
    actorId: id
  };
}

export function userQuery(user = getStoredUser()) {
  const id = encodeURIComponent(userId(user));

  return `userId=${id}&actorId=${id}`;
}

export function actorQuery(user = getStoredUser()) {
  return `actorId=${encodeURIComponent(userId(user))}`;
}

export function getActiveUserId() {
  return userId(getStoredUser());
}

export function getActiveUserName() {
  return getStoredUser()?.name || "";
}

export function getActiveUserEmail() {
  return getStoredUser()?.email || "";
}
