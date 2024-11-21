"use client";

import { useState, useEffect } from "react";
import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import TradeCard from "@/components/trading/trade-card";
import TradeFilters from "@/components/trading/trade-filters";
import { 
  Sheet, 
  SheetContent, 
  SheetTrigger,
  SheetHeader,
  SheetTitle 
} from "@/components/ui/sheet";

const SAMPLE_TRADES = [
  {
    id: 1,
    politician: "Jane Smith",
    symbol: "AAPL",
    company: "Apple Inc.",
    type: "Purchase",
    amount: "$50,000 - $100,000",
    date: "2024-01-15",
    disclosure: "45 days after transaction",
    industry: "Technology",
    amountRange: "50k-100k"
  },
  {
    id: 2,
    politician: "John Doe",
    symbol: "MSFT",
    company: "Microsoft Corporation",
    type: "Sale",
    amount: "$15,000 - $50,000",
    date: "2024-01-10",
    disclosure: "30 days after transaction",
    industry: "Technology",
    amountRange: "15k-50k"
  }
];

export default function TradingPage() {
  const [searchQuery, setSearchQuery] = useState("");
  const [filters, setFilters] = useState({
    type: "all",
    industries: [] as string[],
    amountRanges: [] as string[]
  });
  const [filteredTrades, setFilteredTrades] = useState(SAMPLE_TRADES);

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
    let filtered = SAMPLE_TRADES;

    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(trade =>
        trade.politician.toLowerCase().includes(query) ||
        trade.symbol.toLowerCase().includes(query) ||
        trade.company.toLowerCase().includes(query)
      );
    }

    if (filters.type !== "all") {
      filtered = filtered.filter(trade => 
        trade.type === filters.type
      );
    }

    if (filters.industries.length > 0) {
      filtered = filtered.filter(trade =>
        filters.industries.includes(trade.industry)
      );
    }

    if (filters.amountRanges.length > 0) {
      filtered = filtered.filter(trade =>
        filters.amountRanges.includes(trade.amountRange)
      );
    }

    setFilteredTrades(filtered);
  }, [searchQuery, filters]);

  return (
    <div className="min-h-screen bg-muted/30">
      <div className="bg-background border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8">
          <h1 className="text-2xl sm:text-3xl font-bold mb-4">Stock Trading Activity</h1>
          <p className="text-base sm:text-lg text-muted-foreground mb-6">
            Track and analyze stock trading activities of politicians and their immediate family members.
          </p>
          <form onSubmit={handleSearch} className="flex gap-4">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                type="search"
                placeholder="Search by politician, stock symbol, or company..."
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
                <SheetTitle>Filter Trades</SheetTitle>
              </SheetHeader>
              <div className="mt-6">
                <TradeFilters
                  filters={filters}
                  onFilterChange={handleFilterChange}
                />
              </div>
            </SheetContent>
          </Sheet>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          <aside className="hidden lg:block lg:col-span-1">
            <TradeFilters
              filters={filters}
              onFilterChange={handleFilterChange}
            />
          </aside>
          <main className="lg:col-span-3">
            {filteredTrades.length === 0 ? (
              <div className="text-center text-muted-foreground py-8">
                No trades found matching your criteria
              </div>
            ) : (
              <div className="grid gap-6">
                {filteredTrades.map((trade) => (
                  <TradeCard
                    key={trade.id}
                    politician={trade.politician}
                    symbol={trade.symbol}
                    company={trade.company}
                    type={trade.type}
                    amount={trade.amount}
                    date={trade.date}
                    disclosure={trade.disclosure}
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