const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api";

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...options.headers
    },
    ...options
  });

  if (!response.ok) {
    let message = `请求失败（${response.status}）`;
    try {
      const body = await response.json();
      message = body.message || message;
      if (body.fields) {
        message = Object.values(body.fields)[0] || message;
      }
    } catch {
      // 非 JSON 错误响应沿用状态码信息。
    }
    throw new Error(message);
  }

  return response.status === 204 ? null : response.json();
}

export const attractionApi = {
  list: () => request("/attractions"),
  create: (payload) =>
    request("/attractions", {
      method: "POST",
      body: JSON.stringify(payload)
    }),
  update: (id, payload) =>
    request(`/attractions/${id}`, {
      method: "PUT",
      body: JSON.stringify(payload)
    }),
  remove: (id) =>
    request(`/attractions/${id}`, {
      method: "DELETE"
    }),
  nearby: ({ longitude, latitude, radius }) => {
    const params = new URLSearchParams({
      lng: longitude,
      lat: latitude,
      radius
    });
    return request(`/attractions/nearby?${params}`);
  },
  within: (geometry) =>
    request("/attractions/within", {
      method: "POST",
      body: JSON.stringify(geometry)
    }),
  buffer: (payload) =>
    request("/spatial/buffer", {
      method: "POST",
      body: JSON.stringify(payload)
    }),
  measure: (geometry) =>
    request("/spatial/measure", {
      method: "POST",
      body: JSON.stringify({ geometry })
    }),
  calculateRoute: (attractionIds, optimize) =>
    request("/routes/calculate", {
      method: "POST",
      body: JSON.stringify({ attractionIds, optimize })
    }),
  statistics: () => request("/statistics"),
  health: () => request("/health")
};
