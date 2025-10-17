# Orientation Route Maker - TypeScript

A TypeScript-based orientation route maker with Google Maps integration, featuring distance measurements between waypoints.

## Features

- **Interactive Map**: Click to add waypoints on Google Maps
- **Distance Measurements**: Visual distance labels between waypoints (toggleable)
- **Route Management**: Add, remove, undo/redo waypoints
- **PDF Generation**: Export waypoint data to PDF with coordinates
- **TypeScript**: Full type safety and modern development experience

## Setup

### Prerequisites

- Node.js (v16 or higher)
- npm or yarn

### Installation

1. Install dependencies:
```bash
npm install
```

2. Build the TypeScript code:
```bash
npm run build
```

3. Open `index.html` in your browser

### Development

For development with auto-rebuild:
```bash
npm run watch
```

For development with live server:
```bash
npm run dev
```

## Project Structure

```
├── src/
│   └── app.ts          # Main TypeScript application
├── dist/
│   └── app.js          # Compiled JavaScript (generated)
├── index.html          # Main HTML file
├── tsconfig.json       # TypeScript configuration
├── package.json        # Dependencies and scripts
└── README.md           # This file
```

## Scripts

- `npm run build` - Compile TypeScript to JavaScript
- `npm run watch` - Watch for changes and recompile
- `npm run dev` - Development server with live reload
- `npm run clean` - Remove compiled files

## Usage

1. Open the application in your browser
2. Click on the map to add waypoints
3. Use the control panel to manage your route:
   - Remove Route: Clear all waypoints
   - Invert Route: Reverse waypoint order
   - Remove Last Point: Undo last waypoint
   - Add Last Removed Point: Redo removed waypoint
   - Hide/Show Distances: Toggle distance labels
   - Generate PDF: Export waypoint data

## TypeScript Features

- **Type Safety**: Full type checking for Google Maps API
- **Interface Definitions**: Custom interfaces for waypoints and data structures
- **Modern ES2020**: Latest JavaScript features with type safety
- **Strict Mode**: Enhanced error detection and code quality

## API Integration

- **Google Maps API**: Interactive mapping and geometry calculations
- **PDF Generation**: Both pdfmake and jsPDF support
- **UTM Coordinates**: Automatic coordinate conversion for orientation use

## Browser Support

- Modern browsers with ES2020 support
- Google Maps API compatible browsers
- PDF generation requires modern JavaScript features
