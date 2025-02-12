"use client";

export default function TradingPage() {
  return (
      <div className="min-h-screen bg-muted/30 flex flex-col justify-center items-center text-center px-4">
        <h1 className="text-3xl sm:text-4xl font-bold mb-4">Coming Soon</h1>
        <p className="text-base sm:text-lg text-muted-foreground mb-6">
          We're working on bringing you detailed insights into political trading activity.
          <br />
          In the meantime, tell us what you'd like to see.
        </p>
        <p className="text-base sm:text-lg text-muted-foreground">
          Email us at:{" "}
          <a href="mailto:contact@rankandfile.us" className="text-primary underline">
              contact@rankandfile.us
          </a>
        </p>
      </div>
  );
}
