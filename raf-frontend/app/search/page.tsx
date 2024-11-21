import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Search } from "lucide-react";

export default function SearchPage() {
  return (
    <div className="container mx-auto px-4 py-12">
      <div className="max-w-3xl mx-auto space-y-8">
        <div className="text-center space-y-4">
          <h1 className="text-4xl font-bold">Search Political Data</h1>
          <p className="text-lg text-muted-foreground">
            Find detailed information about legislators, bills, and campaign finances
          </p>
        </div>

        <div className="flex gap-2">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              type="search"
              placeholder="Search by name, state, bill number..."
              className="pl-10"
            />
          </div>
          <Button type="submit">Search</Button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="p-4 rounded-lg border bg-card">
            <h2 className="font-semibold mb-2">Legislators</h2>
            <p className="text-sm text-muted-foreground">
              Search for current and past members of Congress
            </p>
          </div>
          <div className="p-4 rounded-lg border bg-card">
            <h2 className="font-semibold mb-2">Bills</h2>
            <p className="text-sm text-muted-foreground">
              Find legislation and voting records
            </p>
          </div>
          <div className="p-4 rounded-lg border bg-card">
            <h2 className="font-semibold mb-2">Campaign Finance</h2>
            <p className="text-sm text-muted-foreground">
              Track donations and spending
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}