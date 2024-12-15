"use client";

import { useState, useEffect } from "react";
import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import BillCard from "@/components/bills/bill-card";
import BillFilters from "@/components/bills/bill-filters";
import {
  Sheet,
  SheetContent,
  SheetTrigger,
  SheetHeader,
  SheetTitle
} from "@/components/ui/sheet";

const SAMPLE_BILLS = [
  {
    id: 1,
    title: "H.R. 1234 - Clean Energy Act",
    sponsor: "Jane Smith",
    party: "Democratic",
    state: "CA",
    status: "In Committee",
    introducedDate: "2023-09-15",
    summary: "A bill to promote renewable energy development and reduce carbon emissions through federal incentives and regulations.",
    chamber: "house",
    congress: "118th Congress (2023-2024)"
  },
  {
    id: 2,
    title: "S. 789 - Infrastructure Investment Act",
    sponsor: "John Doe",
    party: "Republican",
    state: "TX",
    status: "Passed House",
    introducedDate: "2023-08-22",
    summary: "A comprehensive bill to fund critical infrastructure projects across the United States, including roads, bridges, and public transportation systems.",
    chamber: "senate",
    congress: "118th Congress (2023-2024)"
  }
];

export default function BillsPage() {
  const [searchQuery, setSearchQuery] = useState("");
  const [filters, setFilters] = useState({
    status: "all",
    chamber: [] as string[],
    party: [] as string[],
    congress: [] as string[]
  });
  const [filteredBills, setFilteredBills] = useState(SAMPLE_BILLS);

  const handleFilterChange = (type: string, value: string | string[]) => {
    setFilters(prev => ({
      ...prev,
      [type]: value
    }));
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
  };

  useEffect(() => {
    let filtered = SAMPLE_BILLS;

    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(bill =>
          bill.title.toLowerCase().includes(query) ||
          bill.sponsor.toLowerCase().includes(query) ||
          bill.summary.toLowerCase().includes(query)
      );
    }

    if (filters.status !== "all") {
      filtered = filtered.filter(bill =>
          bill.status === filters.status
      );
    }

    if (filters.chamber.length > 0) {
      filtered = filtered.filter(bill =>
          filters.chamber.includes(bill.chamber)
      );
    }

    if (filters.party.length > 0) {
      filtered = filtered.filter(bill =>
          filters.party.includes(bill.party)
      );
    }

    if (filters.congress.length > 0) {
      filtered = filtered.filter(bill =>
          filters.congress.includes(bill.congress)
      );
    }

    setFilteredBills(filtered);
  }, [searchQuery, filters]);

  return (
      <div className="min-h-screen bg-muted/30">
        <div className="bg-background border-b">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8">
            <h1 className="text-2xl sm:text-3xl font-bold mb-4">Legislative Bills</h1>
            <p className="text-base sm:text-lg text-muted-foreground mb-6">
              Track and analyze current and historical legislation in Congress.
            </p>
            <form onSubmit={handleSearch} className="flex gap-4">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                    type="search"
                    placeholder="Search bills by keyword, number, or sponsor..."
                    className="pl-10"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>
              <Button type="submit">Search</Button>
            </form>
          </div>
        </div>

        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="lg:hidden mb-4">
            <Sheet>
              <SheetTrigger asChild>
                <Button variant="outline" className="w-full">
                  Filters
                </Button>
              </SheetTrigger>
              <SheetContent side="left" className="w-[300px] sm:w-[400px]">
                <SheetHeader>
                  <SheetTitle>Filter Bills</SheetTitle>
                </SheetHeader>
                <div className="mt-6">
                  <BillFilters
                      filters={filters}
                      onFilterChange={handleFilterChange}
                  />
                </div>
              </SheetContent>
            </Sheet>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
            <aside className="hidden lg:block lg:col-span-1">
              <BillFilters
                  filters={filters}
                  onFilterChange={handleFilterChange}
              />
            </aside>
            <main className="lg:col-span-3">
              {filteredBills.length === 0 ? (
                  <div className="text-center text-muted-foreground py-8">
                    No bills found matching your criteria
                  </div>
              ) : (
                  <div className="grid gap-6">
                    {filteredBills.map((bill) => (
                        <BillCard
                            key={bill.id}
                            title={bill.title}
                            sponsor={bill.sponsor}
                            party={bill.party}
                            state={bill.state}
                            status={bill.status}
                            introducedDate={bill.introducedDate}
                            summary={bill.summary}
                        />
                    ))}
                  </div>
              )}
            </main>
          </div>
        </div>
      </div>
  );
}