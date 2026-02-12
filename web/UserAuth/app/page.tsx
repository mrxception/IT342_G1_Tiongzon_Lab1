import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import Link from "next/link";

export default function Home() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-zinc-50 font-sans dark:bg-black">
      <Card className="w-[380px] border border-gray-600 shadow-md">
        <CardHeader className="text-center">
          <CardTitle>User Registration and Authentication</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-4">
          <Link href="/login" className="w-full">
            <Button className="w-full">Login</Button>
          </Link>
          <Link href="/register" className="w-full">
            <Button variant="outline" className="w-full">
              Register
            </Button>
          </Link>
        </CardContent>
      </Card>
    </div>
  );
}