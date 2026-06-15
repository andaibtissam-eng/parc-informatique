const photoBank = [
  {
    match: ["macbook", "apple"],
    url: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=900&q=80"
  },
  {
    match: ["dell", "latitude", "laptop", "portable"],
    url: "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?auto=format&fit=crop&w=900&q=80"
  },
  {
    match: ["thinkpad", "lenovo"],
    url: "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=900&q=80"
  },
  {
    match: ["monitor", "ecran", "display"],
    url: "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=900&q=80"
  },
  {
    match: ["printer", "imprimante"],
    url: "https://images.unsplash.com/photo-1612815154858-60aa4c59eaa6?auto=format&fit=crop&w=900&q=80"
  },
  {
    match: ["server", "serveur", "network", "switch"],
    url: "https://images.unsplash.com/photo-1558494949-ef010cbdcc31?auto=format&fit=crop&w=900&q=80"
  },
  {
    match: ["phone", "mobile", "tablet", "tablette"],
    url: "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=900&q=80"
  }
];

const fallbackPhotos = [
  "https://images.unsplash.com/photo-1593642632823-8f785ba67e45?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1516321497487-e288fb19713f?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1581090464777-f3220bbe1b8b?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1531297484001-80022131f5a1?auto=format&fit=crop&w=900&q=80"
];

export const getEquipmentPhoto = (equipment, index = 0) => {
  if (equipment?.imageUrl) {
    return equipment.imageUrl;
  }

  const searchText = [
    equipment?.name,
    equipment?.brand,
    equipment?.model,
    equipment?.category,
    equipment?.inventoryCode
  ]
    .filter(Boolean)
    .join(" ")
    .toLowerCase();

  const matched = photoBank.find((photo) => photo.match.some((keyword) => searchText.includes(keyword)));
  if (matched) {
    return matched.url;
  }

  return fallbackPhotos[index % fallbackPhotos.length];
};
